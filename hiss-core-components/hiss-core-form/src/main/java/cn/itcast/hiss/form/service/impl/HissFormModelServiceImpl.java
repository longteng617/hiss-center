package cn.itcast.hiss.form.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.HissFormConstants;
import cn.itcast.hiss.api.client.dto.FormServerDefinitionDTO;
import cn.itcast.hiss.api.client.form.*;
import cn.itcast.hiss.common.dtos.Complex;
import cn.itcast.hiss.common.dtos.PageResponseResult;
import cn.itcast.hiss.common.enums.SearchFieldTypeEnum;
import cn.itcast.hiss.common.enums.StatutsType;
import cn.itcast.hiss.form.autofill.AutoFillManager;
import cn.itcast.hiss.form.dto.FormDataDTO;
import cn.itcast.hiss.form.dto.FormDataInfoDTO;
import cn.itcast.hiss.form.dto.FormDetailDTO;
import cn.itcast.hiss.form.mapper.*;
import cn.itcast.hiss.form.mapper.drive.DatabaseDriver;
import cn.itcast.hiss.form.pojo.*;
import cn.itcast.hiss.form.service.HissFormModelService;
import cn.itcast.hiss.form.sql.manage.HissSqlExchangeHandler;
import cn.itcast.hiss.form.sql.manage.HissSqlExchangeManage;
import cn.itcast.hiss.form.validate.ValidateManager;
import cn.itcast.hiss.message.CurrentUser;
import cn.itcast.hiss.message.MessageContext;
import cn.itcast.hiss.message.sender.form.ArgsValidateMessage;
import cn.itcast.hiss.message.sender.form.CreateFormDefinitionMessage;
import cn.itcast.hiss.message.sender.form.FormDataListMessage;
import cn.itcast.hiss.message.sender.form.FormSubmitDataMessage;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.itcast.hiss.form.sql.enums.FunctionEnum.ALTER;
import static cn.itcast.hiss.form.sql.enums.FunctionEnum.CREATE;

/*
 * @author miukoo
 * @description 对表单进行增删改查
 * @date 2023/6/26 14:40
 * @version 1.0
 **/
@Service
@Transactional
@Slf4j
public class HissFormModelServiceImpl implements HissFormModelService {

    @Autowired
    private HissFormModelMapper hissFormModelMapper;
    @Autowired
    private HissFormBytearrayMapper hissFormBytearrayMapper;
    @Autowired
    private HissFormTablesMapper hissFormTablesMapper;
    @Autowired
    private HissFormTableFieldsMapper hissFormTableFieldsMapper;
    @Autowired
    private DatabaseDriver databaseDriver;
    @Autowired
    private DynamicTableMapper dynamicTableMapper;


    /**
     * 保存数据
     * @param message
     * @param messageContext
     */
    @Override
    public void saveFormDefinition(CreateFormDefinitionMessage message, MessageContext messageContext) {
        CreateFormDefinition createFormDefinition = message.getPalyload();
        String tenantId = createFormDefinition.getUserAppId();
        if(StrUtil.isEmpty(tenantId)){
            tenantId = message.getMessageAuth().getTenant();
        }
        String id = createFormDefinition.getId();
        if(StrUtil.isNotEmpty(id)){
            doUpdateAction(createFormDefinition,messageContext,tenantId);
        }else{
            doAddAction(createFormDefinition,messageContext,tenantId);
        }
    }

    /**
     * 查询表单数据
     * @param formDefinition
     * @param messageContext
     * @return
     */
    @Override
    public void queryForm(GetFormDefinition formDefinition, MessageContext messageContext) {
        List<HissFormModel> hissFormModels = queryByGetFormDefinition(formDefinition);
        if(hissFormModels!=null){
            List<FormDefinition> definitions = hissFormModels.stream().map(model -> {
                FormDefinition definition = new FormDefinition();
                definition.setId(model.getId());
                definition.setName(model.getName());
                HissFormBytearray modelJson = hissFormBytearrayMapper.getModelJson(model.getId(), HissFormConstants.FORM_CONFIG_JSON);
                if (modelJson != null) {
                    definition.setContext(modelJson.getContent());
                }
                return definition;
            }).collect(Collectors.toList());
            messageContext.addResultAndCount("res",definitions);
        }else{
            hissFormModels =  new ArrayList<>();
            messageContext.addResultAndCount("res",hissFormModels);
        }
    }

    /**
     * 查询表单数据
     * @param formDefinition
     * @param messageContext
     * @return
     */
    @Override
    public List<FormServerDefinitionDTO>  queryFormInfo(GetFormDefinition formDefinition, MessageContext messageContext) {
        List<HissFormModel> hissFormModels = queryByGetFormDefinition(formDefinition);
        if(hissFormModels!=null){
            return hissFormModels.stream().map(model -> {
                FormServerDefinitionDTO definition = new FormServerDefinitionDTO();
                definition.setId(model.getId());
                definition.setName(model.getName());
                definition.setFields(hissFormTableFieldsMapper.listByModelIdForProcessConfig(model.getId()));
                return definition;
            }).collect(Collectors.toList());
        }else{
            return   new ArrayList<>();
        }
    }

    /**
     * 提交表单数据
     * @param params
     * @param messageContext
     */
    @Override
    public void submitFormData(FormSubmitDataMessage params, MessageContext messageContext) throws Exception {
        FormSubmitData formSubmitData = params.getPalyload();
        String formId = formSubmitData.getFormId();
        HissFormBytearray hissFormBytearray = hissFormBytearrayMapper.getModelJson(formId, HissFormConstants.FORM_CONFIG_JSON);
        List<HissFormTables> hissFormTables = hissFormTablesMapper.listByModelId(formId);
        HissFormTables mainTable = findHissFormTables(hissFormTables, HissFormConstants.FORM_MAIN_TABLE_FLAG);
        List<Complex<String,List>> sqls = new ArrayList<>();
        String dataId = formSubmitData.getDataId();
        // 1、如果没有主数据，则插入一条数据————————新增和修改都是对字段进行修改
        if(StrUtil.isEmpty(formSubmitData.getDataId())){
            dataId = UUID.randomUUID().toString();
            buildInsertSql(params.getMessageAuth().getCurrentUser(),mainTable,dataId,"",sqls);
        }
        // 2、查询所有字段信息
        List<HissFormTableFields> fields = hissFormTableFieldsMapper.listByModelId(formId);
        // 3、转换流程表单配置
        Map<String,Map<String,String>> processConfig = new HashMap<>();
        if(formSubmitData.getFormConfig()!=null) {
            for (FormConfigDTO formConfigDTO : formSubmitData.getFormConfig()) {
                Map<String, String> config = processConfig.getOrDefault(formConfigDTO.getId(), new HashMap<>());
                for (FormConfigFieldDTO field : formConfigDTO.getFields()) {
                    config.put(field.getFieldName(), field.getType());
                }
                processConfig.put(formConfigDTO.getId(), config);
            }
        }
        // 3、每个字段的配置，打平后的数据，用于数据校验
        Map<String, JSONObject> fieldConfig = parseJsonToMap(hissFormBytearray.getContent());
        // 4、记录验证过程中的错误
        Map<String, String> errorMessage = new HashMap<>();
        List<FormPageDTO> formData = formSubmitData.getFormData();
        for (FormPageDTO formDatum : formData) {
            parseAndSaveData(params.getMessageAuth().getCurrentUser(),
                    mainTable,
                    errorMessage,
                    fields,
                    fieldConfig,processConfig,
                    formDatum.getData(),
                    sqls,
                    dataId,formSubmitData.isDraft());
        }
        // 5、校验错误结果
        if(errorMessage.size()>0){
            messageContext.addError("result",errorMessage);
            return;
        }
        // 6、执行过程中所有的sql
        databaseDriver.executeSql(sqls);
        messageContext.addResultAndCount("result",dataId);// 返回主ID
    }

    /**
     * 把控件配置数据打平
     * @param json
     */
    public Map<String,JSONObject> parseJsonToMap(String json){
        Map<String,JSONObject> fieldConfig = new HashMap<>();
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray formDatas = jsonObject.getJSONArray("formData");
        parseFormData(formDatas,fieldConfig);
        return fieldConfig;
    }

    /**
     * 递归提取控件的配置
     * @param formDatas
     * @param map
     */
    private void parseFormData(JSONArray formDatas,Map<String,JSONObject> map){
        if(formDatas!=null){
            for (int i = 0; i < formDatas.size(); i++) {
                JSONObject page = formDatas.getJSONObject(i);
                JSONArray datas = page.getJSONArray("data");
                for (int j = 0; j < datas.size(); j++) {
                    JSONObject field = datas.getJSONObject(j);
                    map.put(field.getString("id"),field);
                    if(field.containsKey("formData")){
                        JSONArray formData = field.getJSONArray("formData");
                        parseFormData(formData,map);
                    }
                }
            }
        }
    }

    /**
     * 插入一条新数据
     * @param currentUser
     * @param mainTable
     * @param dataId
     * @param sqls
     */
    private void buildInsertSql(CurrentUser currentUser,HissFormTables mainTable, String dataId,String parentId, List<Complex<String,List>> sqls) {
        String sql = String.format("insert into `%s` (`id`,`parent_id`) values (?,?);",mainTable.getTablePhysicalName(),parentId);
        sqls.add(new Complex<>(sql,Arrays.asList(dataId,parentId)));
        sql = String.format("update `%s` set `%s`=? where id=? ;",mainTable.getTablePhysicalName(),"user_id");
        sqls.add(new Complex<>(sql,Arrays.asList(currentUser.getUserId(),dataId)));
    }

    /**
     * 解析提交的字段数据，并进行更新
     * @param currentUser   当前登录用户——透传
     * @param mainTable 当前操作的表
     * @param errorMessage  校验有错误的信息——透传
     * @param fields    当前模型所有字段信息（包括子表）——透传
     * @param fieldConfig   当前模型所有字段打平后的字段配置——透传
     * @param processConfig 流程中配置——透传
     * @param data  当前提交的数据
     * @param sqls  记录需要更新的sql——透传
     * @param dataId    当前数据的id
     * @param draft    是否保存为草稿
     */
    private void parseAndSaveData(CurrentUser currentUser,
                                  HissFormTables mainTable,
                                  Map<String, String> errorMessage,
                                  List<HissFormTableFields> fields,
                                  Map<String, JSONObject> fieldConfig,
                                  Map<String,Map<String,String>> processConfig,
                                  Map<String, Object> data,
                                  List<Complex<String,List>> sqls,
                                  String dataId,
                                  Boolean draft) {
        for (String key : data.keySet()) {
            if(errorMessage.size()>0){// 如果存在错误就不继续校验
                return;
            }
            HissFormTableFields hissFormTableFields = findHissFormTableFields(fields, key);
            Object object = data.get(key);
            // 处理一下 多选的值保存问题，多选也是一个数组,把数组的值转成逗号拼接的值
            if(object instanceof JSONArray){
                JSONArray arrys = (JSONArray)object;
                if(arrys.size()>0){
                    Object tempObject = arrys.get(0);
                    if(tempObject instanceof String || tempObject instanceof Integer || tempObject instanceof Long){
                        object = HissFormConstants.JSON_PRIFEX+JSON.toJSONString(arrys);
                    }
                }
            }
            // 如果不是基本类型的数据数组，则为子表单
            if(object instanceof JSONArray){// 子表单需要递归
                if(hissFormTableFields!=null){
                    HissFormTables subTable = hissFormTablesMapper.selectSubTableByParentIdAndFieldId(hissFormTableFields.getTableId(),hissFormTableFields.getControlId());
                    if(subTable!=null){
                        JSONArray arrays = (JSONArray)object;
                        // 删除子表中的数据，以便插入新的数据
                        String sql = String.format("delete from `%s` where parent_id=? ;",subTable.getTablePhysicalName());
                        sqls.add(new Complex<>(sql,Arrays.asList(dataId)));
                        // 循环解析每条数据
                        for (int i = 0; i < arrays.size(); i++) {
                            JSONObject jsonObject = arrays.getJSONObject(i);
                            String subDataId = jsonObject.getString("id");
                            if(StrUtil.isEmpty(subDataId)){
                                subDataId = UUID.randomUUID().toString();
                                buildInsertSql(currentUser,subTable,subDataId,dataId,sqls);
                            }
                            parseAndSaveData(currentUser,subTable,errorMessage,fields,fieldConfig,processConfig,jsonObject,sqls,subDataId,draft);
                        }
                    }
                }
            }else{
                if(hissFormTableFields!=null){
                    // 计算自动填充
                    object = checkAutoFill(hissFormTableFields, object, currentUser);
                    // 计算是否需要更新——依据流程中的配置
                    boolean isUpdate = draft || checkFormConfig(hissFormTableFields,processConfig);
                    if(isUpdate){
                        // 计算基本校验
                        boolean isValidate = draft || checkInputData(fieldConfig,errorMessage,mainTable,dataId,hissFormTableFields,object);
                        if(isValidate){
                            // 添加修改计划
                            String sql = String.format("update `%s` set `%s`=? where id=? ;",mainTable.getTablePhysicalName(),hissFormTableFields.getPhysicalName());
                            sqls.add(new Complex<>(sql,Arrays.asList(object,dataId)));
                        }
                    }
                }
            }
        }
    }

    /**
     * 自动填充字段值
     * @param hissFormTableFields
     * @param object
     * @param currentUser
     * @return
     */
    private Object checkAutoFill(HissFormTableFields hissFormTableFields, Object object, CurrentUser currentUser){
        if( StrUtil.isNotEmpty(hissFormTableFields.getAutoFillType())) {
            if (object == null || StrUtil.isEmpty("" + object)) {
                boolean bool = AutoFillManager.canAutoFill(hissFormTableFields.getAutoFillType());
                if(bool){
                    String newValue = AutoFillManager.autoFill(hissFormTableFields.getAutoFillType(), currentUser);
                    if(StrUtil.isNotEmpty(newValue)){
                        return newValue;
                    }
                }else{
                    throw new RuntimeException("不支持的自动填充类型");
                }
            }
        }
        return object;
    }

    /**
     * 校验字段的基本信息
     * @param fieldConfig
     * @param errorMessage 错误信息
     * @param hissFormTableFields
     * @param object
     * @return
     */
    private boolean checkInputData(Map<String, JSONObject> fieldConfig,
                                   Map<String, String> errorMessage,
                                   HissFormTables mainTable,
                                   String dataId,
                                   HissFormTableFields hissFormTableFields,
                                   Object object) {
        JSONObject config = fieldConfig.get(hissFormTableFields.getControlId());
        if(config!=null){
            String lab = config.getString("lab");
            String type = config.getString("type");
            JSONObject data = config.getJSONObject("data");
            if(data!=null){
                if(data.containsKey("isMust")&&data.getBoolean("isMust")){//验证必填项目
                    if(StrUtil.isBlank(object+"")){
                        errorMessage.put(hissFormTableFields.getId(), String.format("%s是必填项目",lab));
                        return false;
                    }
                }
                if(ValidateManager.canValidate(type)){// 格式验证
                    ArgsValidated argsValidated = ArgsValidated.builder()
                            .args(object == null ? "" : object.toString())
                            .modelId(mainTable.getModelId())
                            .dataId(dataId)
                            .filedId(hissFormTableFields.getId())
                            .type(type).build();
                    boolean validate = ValidateManager.validate(type, argsValidated);
                    if(!validate){
                        errorMessage.put(hissFormTableFields.getId(), String.format("%s格式验证失败",lab));
                        return false;
                    }
                }
                if(data.containsKey("isOnly")&&data.getBoolean("isOnly")){// 唯一性验证
                    if(StrUtil.isNotEmpty(object+"")) {
                        String sql = String.format("select count(1) from %s where id<>'%s' and `%s`='%s'",
                                mainTable.getTablePhysicalName(),
                                dataId,
                                hissFormTableFields.getPhysicalName(),
                                object
                        );
                        int count = databaseDriver.selectCountSql(sql);
                        if(count>0){
                            errorMessage.put(hissFormTableFields.getId(), String.format("%s不唯一，请修改",lab));
                            return false;
                        }
                    }else{
                        errorMessage.put(hissFormTableFields.getId(), String.format("%s需要唯一检查，不能为空",lab));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 校验字段是否需要保存，此配置是在流程中设计的
     * 只有配置为edit的字段才进行保存
     * @param hissFormTableFields
     * @return
     */
    private boolean checkFormConfig(HissFormTableFields hissFormTableFields, Map<String,Map<String,String>> processConfig) {
        Map<String, String> map = processConfig.get(hissFormTableFields.getModelId());
        if(map!=null){
            String str = map.get(hissFormTableFields.getControlId());
            return "edit".equalsIgnoreCase(str);
        }
        // 默认可以保存
        return true;
    }

    /**
     * 查询表单
     * @param formDefinition
     * @return
     */
    private List<HissFormModel> queryByGetFormDefinition(GetFormDefinition formDefinition){
        LambdaQueryWrapper<HissFormModel> wrapper = Wrappers.lambdaQuery();
        int count = 0;
        if(formDefinition!=null){
            if(StrUtil.isNotEmpty(formDefinition.getIds())){
                count++;
                wrapper.in(HissFormModel::getId,formDefinition.getIds());
            }
            if(StrUtil.isNotEmpty(formDefinition.getQuery())){
                count++;
                wrapper.like(HissFormModel::getName,formDefinition.getQuery());
            }
        }
        if(StrUtil.isNotEmpty(formDefinition.getTenantId())) {
            wrapper.eq(HissFormModel::getTenantId, formDefinition.getTenantId());
        }
        wrapper.orderByDesc(HissFormModel::getCreatedTime);
        Page<HissFormModel> page = Page.of(0,10);// 设置最多10条
        page = hissFormModelMapper.selectPage(page, count>0?wrapper:null);
        return page.getRecords();
    }

    /**
     * 修改设置
     * @param createFormDefinition
     * @param messageContext
     * @param tenantId
     */
    private void doUpdateAction(CreateFormDefinition createFormDefinition, MessageContext messageContext, String tenantId) {
        HissFormModel hissFormModel = hissFormModelMapper.selectById(createFormDefinition.getId());
        if(hissFormModel!=null){
            if(tenantId.equalsIgnoreCase(hissFormModel.getTenantId())){
                // 更新基本信息
                HissFormModel model = new HissFormModel();
                BeanUtils.copyProperties(createFormDefinition,model);
                DocumentContext documentContext = JsonPath.parse(createFormDefinition.getFormDetail());
                if(StrUtil.isEmpty(model.getName())){
                    model.setName(documentContext.read("$.baseFormData.formTitle"));
                }
                if(StrUtil.isEmpty(model.getCategory())){
                    model.setCategory(""+documentContext.read("$.baseFormData.groupId"));
                }
                model.setStatus(StatutsType.ENABLE.getCode());
                model.setCreatedTime(null);
                model.setTenantId(null);
                model.setId(hissFormModel.getId());
                hissFormModelMapper.updateById(model);
                // 更新配置json
                HissFormBytearray hissFormBytearray = hissFormBytearrayMapper.getModelJson(model.getId(), HissFormConstants.FORM_CONFIG_JSON);
                if(hissFormBytearray==null){
                    hissFormBytearray = createHissFormBytearray(hissFormModel, createFormDefinition.getFormDetail());
                }else{
                    hissFormBytearray.setRev(hissFormBytearray.getRev()+1);
                    hissFormBytearray.setContent(createFormDefinition.getFormDetail());
                    hissFormBytearrayMapper.updateById(hissFormBytearray);
                }
                // 3、解析数据并生成表
                parseJsonAndSave(hissFormModel,hissFormBytearray,messageContext,tenantId);
                messageContext.addResultAndCount("msg","保存成功");
            }else{
                messageContext.addError("msg","无权限修改");
            }
        }else{
            messageContext.addError("msg","修改的模型不存在");
        }
    }

    /**
     * 新增设计
     * @param createFormDefinition
     * @param messageContext
     */
    private void doAddAction(CreateFormDefinition createFormDefinition, MessageContext messageContext,String tenantId) {
        // 1、保存数据
        HissFormModel hissFormModel = new HissFormModel();
        BeanUtils.copyProperties(createFormDefinition,hissFormModel);
        DocumentContext documentContext = JsonPath.parse(createFormDefinition.getFormDetail());
        if(StrUtil.isEmpty(hissFormModel.getName())){
            hissFormModel.setName(documentContext.read("$.baseFormData.formTitle"));
        }
        if(StrUtil.isEmpty(hissFormModel.getCategory())){
            hissFormModel.setCategory(""+documentContext.read("$.baseFormData.groupId"));
        }
        if (StrUtil.isEmpty(hissFormModel.getVersion())) {
            hissFormModel.setVersion("1.0.0");
        }
        hissFormModel.setStatus(StatutsType.ENABLE.getCode());
        hissFormModel.setCreatedTime(new Date());
        hissFormModel.setTenantId(tenantId);
        hissFormModelMapper.insert(hissFormModel);
        // 2、保存配置数据
        HissFormBytearray hissFormBytearray = createHissFormBytearray(hissFormModel, createFormDefinition.getFormDetail());
        // 3、解析数据并生成表
        parseJsonAndSave(hissFormModel,hissFormBytearray,messageContext,tenantId);
        messageContext.addResultAndCount("msg","保存成功");
    }

    /**
     * 创建配置数据
     * @param hissFormModel
     */
    private HissFormBytearray createHissFormBytearray(HissFormModel hissFormModel,String content){
        HissFormBytearray hissFormBytearray = new HissFormBytearray();
        hissFormBytearray.setName(HissFormConstants.FORM_CONFIG_JSON);
        hissFormBytearray.setRev(0);
        hissFormBytearray.setDataId(hissFormModel.getId());
        hissFormBytearray.setContent(content);
        hissFormBytearrayMapper.insert(hissFormBytearray);
        return hissFormBytearray;
    }

    /**
     * 解析JSON并创建表和记录字段信息
     * @param hissFormModel
     * @param hissFormBytearray
     * @param messageContext
     * @param tenantId
     */
    private void parseJsonAndSave(HissFormModel hissFormModel,HissFormBytearray hissFormBytearray,MessageContext messageContext,String tenantId){
        FormDetailDTO baseFromDataDTO = JSON.parseObject(hissFormBytearray.getContent(), FormDetailDTO.class);
        // 生成主表
        List<HissFormTables> hissFormTables = hissFormTablesMapper.listByModelId(hissFormModel.getId());
        List<HissFormTables> useTables = new ArrayList<>();// 使用过的表
        HissFormTables mainTables = createTable(useTables,hissFormModel,hissFormTables,HissFormConstants.FORM_MAIN_TABLE_FLAG,null,null);
        // 递归给主表添加字段
        List<FormDataInfoDTO> tempList = new ArrayList<>();
        for (FormDataDTO page : baseFromDataDTO.getFormData()) {
            tempList.addAll(page.getData());
        }
        addFileds(useTables,hissFormModel,hissFormTables,mainTables, tempList,0);
        // 标识无效的表
        // 标识无效的字段
        for (HissFormTables tempT : useTables) {
            hissFormTables.remove(tempT);
        }
        for (HissFormTables hissFormTable : hissFormTables) {
            hissFormTable.setStatus(StatutsType.DISABLE.getCode());
            hissFormTablesMapper.updateById(hissFormTable);
        }
    }

    /**
     * 递归增加字段
     * @param mainTables
     * @param formData
     */
    private void addFileds(List<HissFormTables> useTables,HissFormModel hissFormModel,List<HissFormTables> hissFormTables,HissFormTables mainTables,List<FormDataInfoDTO> formData,int level) {
        List<HissFormTableFields> hissFormTableFieldsList = hissFormTableFieldsMapper.listByTableId(mainTables.getId());
        List<HissFormTableFields> tempFields = new ArrayList<>();
        for (FormDataInfoDTO field : formData) {
            // 插入或更新
            HissFormTableFields hissFormTableFields = findHissFormTableFields(hissFormTableFieldsList, field.getId());
            String title = field.getTitle();
            if(field.getData()!=null&&StrUtil.isNotEmpty(field.getData().getTitle())){
                title = field.getData().getTitle();
            }
            if(hissFormTableFields==null){
                hissFormTableFields = new HissFormTableFields();
                hissFormTableFields.setLevel(level);
                hissFormTableFields.setStatus(StatutsType.ENABLE.getCode());
                hissFormTableFields.setTableId(mainTables.getId());
                hissFormTableFields.setFieldType(field.getType());
                hissFormTableFields.setFieldName(title);
                hissFormTableFields.setSearchDisplay("input".equals(field.getType()));
                hissFormTableFields.setSearchType(SearchFieldTypeEnum.LIKE);
                hissFormTableFields.setCreatedTime(new Date());
                hissFormTableFields.setListDisplay(true);
                hissFormTableFields.setPhysicalName(genFieldName(field.getId()));
                hissFormTableFields.setControlId(field.getId());
                hissFormTableFields.setModelId(hissFormModel.getId());
                if(field.getData()!=null){
                    hissFormTableFields.setIsOnly(field.getData().getOnlyFlag());
                    hissFormTableFields.setSearchType(field.getData().getSearchType());
                    hissFormTableFields.setListDisplay(field.getData().getListDisplay());
                    hissFormTableFields.setSearchDisplay(field.getData().getSearchDisplay());
                }
                hissFormTableFieldsMapper.insert(hissFormTableFields);
                createTablePhysicalField(mainTables,hissFormTableFields);
            }else{
                if(field.getData()!=null){
                    hissFormTableFields.setSearchType(field.getData().getSearchType());
                    hissFormTableFields.setListDisplay(field.getData().getListDisplay());
                    hissFormTableFields.setSearchDisplay(field.getData().getSearchDisplay());
                    hissFormTableFields.setIsOnly(field.getData().getOnlyFlag());
                }
                hissFormTableFields.setStatus(StatutsType.ENABLE.getCode());
                hissFormTableFields.setFieldType(field.getType());
                hissFormTableFields.setFieldName(title);
                hissFormTableFieldsMapper.updateById(hissFormTableFields);
                tempFields.add(hissFormTableFields);
            }
            // 检查是否需要递归
            FormDataInfoDTO data = field.getData();
            if(data!=null){
                List<FormDataInfoDTO> tempData = data.getFormData();
                if (tempData != null && tempData.size() > 0) {
                    HissFormTables subTables = createTable(useTables, hissFormModel, hissFormTables, field.getId(), mainTables, field);
                    addFileds(useTables, hissFormModel, hissFormTables, subTables, tempData, level + 1);
                }
            }
        }
        // 标识无效的字段
        for (HissFormTableFields tempField : tempFields) {
            hissFormTableFieldsList.remove(tempField);
        }
        for (HissFormTableFields hissFormTableFields : hissFormTableFieldsList) {
            hissFormTableFields.setStatus(StatutsType.DISABLE.getCode());
            hissFormTableFieldsMapper.updateById(hissFormTableFields);
        }
    }

    /**
     * 判断是否存在表，如果不存在则自动创建一张表
     * @param hissFormModel
     * @param hissFormTables
     * @param key
     * @return
     */
    private HissFormTables createTable(List<HissFormTables> useTables,HissFormModel hissFormModel,List<HissFormTables> hissFormTables,String key,HissFormTables parentTable,FormDataInfoDTO field){
        HissFormTables mainTables = findHissFormTables(hissFormTables,key);
        if(mainTables==null){
            mainTables = new HissFormTables();
            mainTables.setCreatedTime(new Date());
            mainTables.setFlag(key);
            mainTables.setTableName(hissFormModel.getName());
            mainTables.setTablePhysicalName(genTableName());
            mainTables.setModelId(hissFormModel.getId());
            if(parentTable!=null){
                mainTables.setParentId(parentTable.getId());
            }
            if(field!=null){
                mainTables.setFieldId(field.getId());
            }
            hissFormTablesMapper.insert(mainTables);
            createTablePhysical(hissFormModel,mainTables);
        }else{
            useTables.add(mainTables);
        }
        return mainTables;
    }

    /**
     * 创建物理表
     * @param hissFormModel
     * @param mainTables
     */
    private void createTablePhysical(HissFormModel hissFormModel,HissFormTables mainTables) {
        FormDetailPojo formDetailPojo = new FormDetailPojo();
        formDetailPojo.setModelId(hissFormModel.getId());
        formDetailPojo.setId(mainTables.getId());
        formDetailPojo.setFields(new ArrayList<>());
        formDetailPojo.setTableName(mainTables.getTablePhysicalName());
        HissSqlExchangeHandler sqlExchangeHandler = HissSqlExchangeManage.getSqlExchangeHandler(CREATE);
        String createSql = sqlExchangeHandler.exchangeSql(formDetailPojo);
        log.info("往表单的主表执行插入的检表语句为：{}", createSql);
        databaseDriver.executeSql(createSql);
    }

    /**
     * 创建物理表
     * @param mainTables
     * @param hissFormTableFields
     */
    private void createTablePhysicalField(HissFormTables mainTables,HissFormTableFields hissFormTableFields) {
        FormDetailPojo formDetailPojo = new FormDetailPojo();
        formDetailPojo.setFields(Arrays.asList(hissFormTableFields));
        formDetailPojo.setTableName(mainTables.getTablePhysicalName());
        HissSqlExchangeHandler sqlExchangeHandler = HissSqlExchangeManage.getSqlExchangeHandler(ALTER);
        String createSql = sqlExchangeHandler.exchangeSql(formDetailPojo);
        log.info("往表单的主表执行插入的检表语句为：{}", createSql);
        databaseDriver.executeSql(createSql);
    }

    private String genFieldName(String id){
        return String.format("hf_%s",id);
    }

    private String genTableName(){
        return String.format("hf_%s",System.currentTimeMillis());
    }

    /**
     * 查找字段标识
     * @param hissFormTables
     * @param key
     * @return
     */
    public HissFormTableFields findHissFormTableFields(List<HissFormTableFields> hissFormTables,String key ){
        for (HissFormTableFields hissFormTable : hissFormTables) {
            if(key.equals(hissFormTable.getControlId())){
                return hissFormTable;
            }
        }
        return null;
    }

    /**
     * 提供的通用验证接口
     * @param params
     * @param messageContext
     */
    @Override
    public void argsValidated(ArgsValidateMessage params, MessageContext messageContext) {
        ArgsValidated palyload = params.getPalyload();
        if(ValidateManager.canValidate(palyload.getType())){
            boolean validate = ValidateManager.validate(palyload.getType(), palyload);
            messageContext.addError("result",validate);
        }else{
            messageContext.addError("msg","不支持的验证类型");
        }
    }

    /**
     * 获取一条表单的数据
     * @param params
     * @param messageContext
     */
    @Override
    public void queryFormData(FormSubmitDataMessage params, MessageContext messageContext) {
        FormSubmitData palyload = params.getPalyload();
        HissFormModel hissFormModel = hissFormModelMapper.selectById(palyload.getFormId());
        if(hissFormModel!=null){
            List<HissFormTables> hissFormTables = hissFormTablesMapper.listByModelId(hissFormModel.getId());
            List<HissFormTableFields> fields = hissFormTableFieldsMapper.listByModelId(hissFormModel.getId());
            Map<String, Map<String, String>> tableFieldMapping = fields.stream().collect(Collectors.groupingBy(HissFormTableFields::getTableId, Collectors.toMap(HissFormTableFields::getPhysicalName, HissFormTableFields::getControlId)));
            if(hissFormTables!=null){
                HissFormTables mainTable = findHissFormTables(hissFormTables, HissFormConstants.FORM_MAIN_TABLE_FLAG);
                Map map = dynamicTableMapper.listTableDataById(mainTable, palyload.getDataId());
                if( map==null ){ // 如果没有数据则进行自动填充
                    map = new HashMap();
                }
                Map<String, Object> data = mapToResult(tableFieldMapping, map, mainTable.getId());
                findSubTableData(mainTable,data,tableFieldMapping,hissFormTables);
                messageContext.addResultAndCount("result",data);
            }else{
                messageContext.addError("msg","未找到对应的表单存储表信息");
            }
        }else{
            messageContext.addError("msg","未找到对应的表单");
        }
    }

    @Override
    public void deleteByModelId(String modelId) {
        hissFormModelMapper.deleteById(modelId);
        hissFormBytearrayMapper.deleteByDataId(modelId);

        // 删除表
        List<HissFormTables> hissFormTables = hissFormTablesMapper.listByModelId(modelId);
        hissFormTablesMapper.deleteByModelId(modelId);

        // 删除字段信息
        hissFormTableFieldsMapper.deleteByModelId(modelId);

        // 删除物理表
        if(hissFormTables!=null){
            for (HissFormTables hissFormTable : hissFormTables) {
                String sql = String.format("drop table `%s`",hissFormTable.getTablePhysicalName());
                databaseDriver.executeSql(sql);
            }
        }
    }

    /**
     * 递归找到相关的数据
     * @param formDefinition
     * @param messageContext
     */
    @Override
    public void queryFormField(GetFormDefinition formDefinition, MessageContext messageContext) {
        List<HissFormModel> hissFormModels = queryByGetFormDefinition(formDefinition);
        if(hissFormModels!=null){
            for (HissFormModel hissFormModel : hissFormModels) {
                List<HissFormTables> hissFormTables = hissFormTablesMapper.listByModelId(hissFormModel.getId());
                HissFormTables mainTable = findHissFormTables(hissFormTables, HissFormConstants.FORM_MAIN_TABLE_FLAG);
                List<HissFormTableFields> fields = hissFormTableFieldsMapper.listByTableId(mainTable.getId());
                mainTable.setFields(fields);
                mainTable.setTablePhysicalName("");
                setTableChild(mainTable,hissFormTables);
                messageContext.addResultAndCount("result",mainTable);
            }
            if(hissFormModels.size()==0){
                messageContext.addError("msg","未找到对应表单");
            }
        }else{
            messageContext.addError("msg","未找到对应表单");
        }
    }

    /**
     * 把一条数据复制一行
     * @param formId
     * @param oldDataId
     * @param newDataId
     */
    @Override
    public void copyFormData(String formId, String oldDataId, String newDataId) {
        List<HissFormTables> hissFormTables = hissFormTablesMapper.listByModelId(formId);
        HissFormTables mainTable = findHissFormTables(hissFormTables, HissFormConstants.FORM_MAIN_TABLE_FLAG);
        List<HissFormTableFields> fields = hissFormTableFieldsMapper.listByTableId(mainTable.getId());
        String parentId = "";// 父数据ID
        dynamicTableMapper.copyRowData(mainTable,fields, oldDataId, newDataId,parentId);
        copySubTableData(mainTable,oldDataId,newDataId,hissFormTables);
    }

    @Override
    public void queryAutoFillData(FormSubmitDataMessage params, MessageContext messageContext) {
        FormSubmitData palyload = params.getPalyload();
        HissFormModel hissFormModel = hissFormModelMapper.selectById(palyload.getFormId());
        if(hissFormModel!=null){
            HissFormTableFields field = hissFormTableFieldsMapper.getByModelIdAndFieldId(hissFormModel.getId(),palyload.getControlId());
            if(field!=null){
                Object temp = checkAutoFill(field, null, params.getMessageAuth().getCurrentUser());
                if(temp==null){
                    temp = "";
                }
                messageContext.addResultAndCount("result",temp);
            }else{
                messageContext.addError("msg","未找到对应的表单字段信息");
            }
        }else{
            messageContext.addError("msg","未找到对应的表单");
        }
    }

    /**
     * 拷贝子表的数据
     * @param mainTable
     * @param parentOldDataId
     * @param parentNewDataId
     * @param hissFormTables
     */
    private void copySubTableData(HissFormTables mainTable, String parentOldDataId, String parentNewDataId, List<HissFormTables> hissFormTables) {
        for (HissFormTables hissFormTable : hissFormTables) {
            if(mainTable.getId().equals(hissFormTable.getParentId())){
                List<HissFormTableFields> fields = hissFormTableFieldsMapper.listByTableId(hissFormTable.getId());
                List<Map> results = dynamicTableMapper.listTableDataByParentId(hissFormTable, parentOldDataId);
                if(results!=null){
                    for (Map result : results) {
                        String oldDataId = (String) result.get("id");
                        String newDataId = UUID.randomUUID().toString();
                        dynamicTableMapper.copyRowData(hissFormTable,fields, oldDataId,newDataId,parentNewDataId);
                        copySubTableData(hissFormTable,oldDataId,newDataId,hissFormTables);//递归拷贝
                    }
                }
            }
        }
    }

    /**
     * 递归查找子表
     * @param parentTable
     * @param hissFormTables
     */
    private void setTableChild(HissFormTables parentTable, List<HissFormTables> hissFormTables) {
        List<HissFormTables> childs = new ArrayList<>();
        for (HissFormTables hissFormTable : hissFormTables) {
            if(parentTable.getId().equals(hissFormTable.getParentId())){
                List<HissFormTableFields> fields = hissFormTableFieldsMapper.listByTableId(hissFormTable.getId());
                hissFormTable.setFields(fields);
                setTableChild(hissFormTable,hissFormTables);
                hissFormTable.setTablePhysicalName("");
                childs.add(hissFormTable);
            }
        }
        parentTable.setChilds(childs);
    }

    /**
     * 递归加载子表存储的数据
     * @param parentTable
     * @param parentData
     * @param tableFieldMapping
     * @param hissFormTables
     */
    private void findSubTableData(HissFormTables parentTable,Map<String, Object> parentData,Map<String, Map<String, String>> tableFieldMapping,List<HissFormTables> hissFormTables){
        for (HissFormTables hissFormTable : hissFormTables) {
            if(parentTable.getId().equals(hissFormTable.getParentId())){
                List<Map> results = dynamicTableMapper.listTableDataByParentId(hissFormTable, (String) parentData.get("id"));
                List<Map<String, Object>> list = new ArrayList<>();
                for (Map result : results) {
                    Map<String, Object> data = mapToResult(tableFieldMapping, result, hissFormTable.getId());
                    findSubTableData(hissFormTable,data,tableFieldMapping,hissFormTables);
                    list.add(data);
                }
                parentData.put(hissFormTable.getFieldId(),list);
            }
        }
    }

    /**
     * 把物理字段的集合，转成前端可识别的id字段集合
     * @param tableFieldMapping
     * @param map
     * @param tableId
     * @return
     */
    private Map<String,Object> mapToResult( Map<String, Map<String, String>> tableFieldMapping,Map map,String tableId){
        Map<String, String> mapping = tableFieldMapping.get(tableId);
        Map<String,Object> data = new HashMap<>();
        if(mapping!=null){
            for (String physicalName : mapping.keySet()) {
                String id = mapping.get(physicalName);
                if(id!=null){
                    Object value = map.get(physicalName);
                    if(value!=null && value instanceof String){
                        String str = (String)value;
                        if(str.startsWith(HissFormConstants.JSON_PRIFEX)){
                            str = str.substring(HissFormConstants.JSON_PRIFEX.length());
                            if(str.startsWith("[")){
                                value = JSON.parseArray(str);
                            }else{
                                value = JSON.parse(str);
                            }
                        }
                    }
                    data.put(id,value);
                }
            }
        }
        data.put("id",map.get("id"));
        return data;
    }

    /**
     * 查找表的标识
     * @param hissFormTables
     * @param key
     * @return
     */
    private HissFormTables findHissFormTables(List<HissFormTables> hissFormTables,String key ){
        for (HissFormTables hissFormTable : hissFormTables) {
            if(key.equals(hissFormTable.getFlag())){
                return hissFormTable;
            }
        }
        return null;
    }

}
