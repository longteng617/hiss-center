<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24" :xs="24" style="text-align: right">
        <el-col :span="1.5">
          <el-radio-group v-model="searchData.status" @change="handleQuery">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PREPARE">待发起</el-radio-button>
            <el-radio-button label="ACTIVE">办理中</el-radio-button>
            <el-radio-button label="COMPLETE">已完成</el-radio-button>
            <el-radio-button label="CANCEL">已取消</el-radio-button>
          </el-radio-group>
        </el-col>
        <el-form :model="searchData" ref="queryForm" size="small" :inline="true" label-width="68px">
          <el-form-item label="流程名：" prop="name">
            <el-input
              v-model="searchData.name"
              placeholder="请输入流程名"
              clearable
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="processList">
      <el-table-column type="index" width="50" align="center"/>
      <el-table-column label="流程名" align="center" key="name" prop="name"/>
      <el-table-column label="申请时间" align="center" key="startTime" prop="dept.startTime"
                       :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" key="status">
        <template slot-scope="scope">
          {{ statusList[scope.row.status].label }}
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
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看
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
import {getProcessInstanceList, delProcessInstance} from '@/api/process'

export default {
  name: "index",
  data() {
    return {
      loading: false,
      total: 0,
      processList: [],
      statusList: PROCESS_APPLAY_STATUS,
      searchData: {
        status: "",
        name: '',
        businessKey: '',
        pageSize: 10,
        pageNum: 1
      }
    }
  },
  mounted() {
    this.getList()
  },
  methods: {
    handleDelete(row) {
      this.$modal.confirm('是否确认删除名称为"' + row.name + '"的数据？')
        .then(async () => {
          console.log(row)
          const res = await delProcessInstance(row.id)
          if (res.code === 200) {
            this.$modal.msgSuccess("删除成功");
            await this.getList();
          }else{
            this.$modal.msgError("删除失败："+res.msg);
          }
        }).catch(() => {})
    },
    handleView(row) {
      const params = {id: row.id}
      this.$router.push({name: 'InstanceHandle', query: params})
    },
    handleQuery() {
      this.searchData.pageNum = 1
      this.getList()
    },
    async getList() {
      this.loading = false
      try {
        const res = await getProcessInstanceList(this.searchData) // 获取列表数据
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
