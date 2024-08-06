<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24" :xs="24" style="text-align: right">
        <el-form :model="searchData" ref="queryForm" size="small" :inline="true" label-width="68px">
          <el-form-item label="流程名：" prop="name">
            <el-input
              v-model="searchData.name"
              placeholder="请输入流程名"
              clearable
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item label="业务号：" prop="businessKey">
            <el-input
              v-model="searchData.businessKey"
              placeholder="请输入业务号"
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
      <el-table-column label="业务号" align="center" key="businessKey" prop="businessKey"
                       :show-overflow-tooltip="true"/>
      <el-table-column label="耗时" align="center" key="duration" prop="duration" :show-overflow-tooltip="true"/>
      <el-table-column label="申请时间" align="center" key="startTime" prop="dept.startTime"
                       :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="完成时间" align="center" prop="endTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
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
          >{{ opName }}
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
import {getUserHandleProcessList} from '@/api/process'
export default {
  name: "HandleList",
  props:{
    status: {
      type: String,
      default: 'ACTIVE'
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
      statusList: PROCESS_APPLAY_STATUS,
      searchData: {
        status: this.status,
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
    handleView(row) {
      const params = {id: row.id}
      this.$router.push({path: 'handleBis',meta:{ title:'[办理]'+row.name }, query: params})
    },
    handleQuery() {
      this.searchData.pageNum = 1
      this.getList()
    },
    async getList() {
      this.loading = false
      try {
        const res = await getUserHandleProcessList(this.searchData) // 获取列表数据
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
