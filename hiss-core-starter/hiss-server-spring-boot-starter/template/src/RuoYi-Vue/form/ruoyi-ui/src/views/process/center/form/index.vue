<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24" :xs="24" style="text-align: right">
        <el-form :model="searchData" ref="queryForm" size="small" :inline="true" label-width="68px">
          <el-form-item label="表单名：" prop="name">
            <el-input
              v-model="searchData.name"
              placeholder="请输入表单名"
              clearable
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item label="流程分类：" prop="category" label-width="88px">
            <el-select v-model="searchData.category">
              <el-option v-for="item in formCategoryList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-reset" plain size="mini" @click="handleReset">重置</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="danger" icon="el-icon-plus" size="mini" @click="handleAdd">新建表单</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="processList">
      <el-table-column type="index" width="50" align="center"/>
      <el-table-column label="表单名称" align="center" key="name" prop="name"/>
      <el-table-column label="创建时间" align="center" key="createdTime" prop="dept.createdTime"
                       :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdTime) }}</span>
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
            @click="handleEdit(scope.row)"
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
import {PROCESS_APPLAY_STATUS,getMessageAuth} from '@/constants/index';
import {getFormList, delFormModel, getFormCategoryList} from '@/api/process'
export default {
  name: "index",
  data() {
    return {
      loading: false,
      total: 0,
      formCategoryList:[],
      processList: [],
      searchData: {
        name: '',
        category: '',
        pageSize: 10,
        pageNum: 1
      }
    }
  },
  mounted() {
    this.getList()
    this.getCategory()
  },
  methods: {
    handleAdd(){
      if(this.searchData.category){
        const messageAuth = getMessageAuth()
        const params = { category: this.searchData.category, tenant: messageAuth.tenant }
        this.$modal.msgSuccess("功能稍后实现");
      }else{
        this.$modal.msgError("请先选择一个流程分类后再点击新增");
      }
    },
    handleReset() {
      this.searchData = {
        name: '',
        category: '',
        pageSize: 10,
        pageNum: 1
      }
    },
    async getCategory() {
      const res = await getFormCategoryList({}) // 获取列表数据
      this.formCategoryList = res.data
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除名称为"' + row.name + '"的数据？')
        .then(async () => {
          console.log(row)
          const res = await delFormModel(row.id)
          if (res.code === 200) {
            this.$modal.msgSuccess("删除成功");
            await this.getList();
          }else{
            this.$modal.msgError("删除失败："+res.msg);
          }
        }).catch(() => {})
    },
    handleEdit(row) {
      this.$modal.msgSuccess("功能稍后实现");
      // const params = {id: row.id}
      // this.$router.push({name: 'handleBis', query: params})
    },
    handleQuery() {
      this.searchData.pageNum = 1
      this.getList()
    },
    async getList() {
      this.loading = false
      try {
        const res = await getFormList(this.searchData) // 获取列表数据
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

</style>
