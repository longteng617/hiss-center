<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24" :xs="24" style="text-align: right">
        <el-form :model="searchData" ref="queryForm" size="small" :inline="true" label-width="88px">
          <el-form-item label="流程名：" prop="name">
            <el-input
              v-model="searchData.name"
              placeholder="请输入流程名"
              clearable
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item label="流程分类：" prop="category">
            <el-select v-model="searchData.category">
              <el-option v-for="item in processCategoryList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-reset" plain size="mini" @click="handleReset">重置</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="danger" icon="el-icon-plus" size="mini" @click="handleAdd">新增流程</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="processList">
      <el-table-column type="index" width="50" align="center"/>
      <el-table-column label="" align="center" width="80" key="icon" prop="icon">
        <template slot-scope="scope">
          <img v-if="scope.row.icon" :src="scope.row.icon" />
          <svg-icon v-else icon-class="swagger" class="hiss-default-icon" />
        </template>
      </el-table-column>
      <el-table-column label="流程名" align="center" key="name" prop="name"/>
      <el-table-column label="创建时间" align="center" key="createTime" prop="createTime"
                       :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="修改时间" align="center" prop="lastUpdateTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastUpdateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        width="160"
        class-name="small-padding fixed-width"
      >
        <template slot-scope="scope" v-if="scope.row.userId !== 1">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleView(scope.row.id)"
          >编辑
          </el-button>

          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="searchData.pageNum"
      :limit.sync="searchData.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import {PROCESS_APPLAY_STATUS} from '@/constants/index';
import {getProcessDevList,getProcessBisList,getProcessCategoryList,delProcessModel} from '@/api/process'
import {getMessageAuth} from "@/constants";
export default {
  name: "HandleList",
  props:{
    mode: {
      type: String,
      default: 'Dev'
    },
    opName: {
      type: String,
      default: '查看'
    }
  },
  data() {
    return {
      loading: false,
      total: 0,
      processList: [],
      processCategoryList:[],
      searchData: {
        name: '',
        category: '',
        pageSize: 10,
        pageNum: 1
      }
    }
  },
  mounted() {
    this.getCategory()
    this.getList()
  },
  methods: {
    handleDelete(row){
      this.$modal.confirm('是否确认删除名称为"' + row.name + '"的数据？')
        .then(async () => {
          const res = await delProcessModel(row.id)
          if (res.code === 200) {
            this.$modal.msgSuccess("删除成功");
            await this.getList();
          }else{
            this.$modal.msgError("删除失败："+res.msg);
          }
        }).catch(() => {})
    },
    handleReset(){
      this.searchData={
        name: '',
        category: '',
        pageSize: 10,
        pageNum: 1
      }
      this.getList()
    },
    async getCategory() {
      const res = await getProcessCategoryList({}) // 获取列表数据
      this.processCategoryList = res.data
    },
    handleAdd(){
      if(this.searchData.category){
        const messageAuth = getMessageAuth()
        const params = { category: this.searchData.category, tenant: messageAuth.tenant }
        if(this.mode=='Dev'){
          this.$router.push({name: 'DesignDevAdd', query: params})
        }else{
          this.$router.push({name: 'DesignBisAdd', query: params})
        }
      }else{
        this.$modal.msgError("请先选择一个流程分类后再点击新增");
      }
    },
    handleView(id) {
      const params = {}
      if(id){
        params.id = id
      }
      if(this.mode=='Dev'){
        this.$router.push({name: 'DesignDevEdit', query: params})
      }else{
        this.$router.push({name: 'DesignBisEdit', query: params})
      }
    },
    handleQuery() {
      this.searchData.pageNum = 1
      this.getList()
    },
    async getList() {
      this.loading = false
      try {
        let res = null
        if(this.mode =='Dev'){
          res = await getProcessDevList(this.searchData) // 获取列表数据
        }else{
          res = await getProcessBisList(this.searchData) // 获取列表数据
        }
        this.processList = res.data.data
        this.total = Number(res.data.total)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.hiss-default-icon {
  width: 42px;
  height: 42px;
  color: #FFFFFF;
  font-weight: bold;
  border: 1px crimson solid;
  background-color: crimson;
  border-radius: 5px;
  padding: 12px;
}
</style>
