<!-- 预约等级列表页 -->
<template>
  <div class="min-h subscribe bg-wt">
    <!-- 筛选区域 -->
    <SearchFormBox
      :search-data="pagination"
      @handle-search="handleSearch"
      @handle-reset="handleReset"
    ></SearchFormBox>
    <!-- end -->
    <!-- tab导航 -->
    <div class="marg-main newBox">
      <SwitchBar
        ref="tabBar"
        :data="hissProcessStatus"
        @change-id="changeId"
      ></SwitchBar>
    </div>
    <!-- 表格 -->
    <TableList
      :list-data="listData"
      :pagination="pagination"
      :total="total"
      @get-current="getCurrent"
      @handleDelete="handleDelete"
    ></TableList>
    <!-- end -->
    <t-dialog
      v-model:visible="deleteVisible"
      header="确认"
      body="确认要删除流程吗？"
      attach="body"
      :confirm-on-enter="true"
      :on-confirm="onConfirmDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
// 基本数据
import { hissProcessStatus } from '@/utils/commonData'
// 接口
// tab切换
import SwitchBar from '@/components/switchBar/switchBar.vue'
// 表格列表
import TableList from './components/TableList.vue'
// 搜索框表单
import SearchFormBox from './components/SearchForm.vue'
import {delUserApplayProcess, getUserApplayProcessList} from '@/api/process'
import {NotifyPlugin} from "tdesign-vue-next";
// ------定义变量------
const listData = ref([]) // 列表数据
const dataLoading = ref(false) // 加载中
const total = ref(0) // 总条数

// 分页
const pagination = ref<Object | any>({
  pageSize: 10,
  pageNum: 1 // 默认当前页
})
// 生命周期
onMounted(() => {
  getList()
})
// ------定义方法------
// 获取列表数据
const getList = async () => {
  dataLoading.value = false
  try {
    const res: any = await getUserApplayProcessList(pagination.value) // 获取列表数据
    listData.value = res.data.data
    total.value = Number(res.data.total)
  } finally {
    dataLoading.value = false
  }
}
// 搜索功能
const handleSearch = (time) => {
  if (time.length > 0) {
    pagination.value.startTime = new Date(time[0]).getTime()
    pagination.value.endTime = new Date(time[1]).getTime()
  }
  getList()
}
// 重置，清空搜索框
const handleReset = () => {
  // 重置页码
  pagination.value = {
    pageSize: 10,
    pageNum: 1
  }
  getList()
}
// tab筛选
const changeId = (val) => {
  pagination.value.pageNum = 1
  pagination.value.status = val
  getList()
}
// 翻页设置当前页
const getCurrent = (val) => {
  pagination.value.pageNum = val.current
  pagination.value.pageSize = val.pageSize
  getList()
}
const deleteVisible = ref(false)
const deleteId = ref('')
// 删除
const handleDelete = (id) => {
  deleteId.value = id
  deleteVisible.value = true
}
// 关闭发起申请弹层
const onConfirmDelete = async () => {
  dataLoading.value = false
  try {
    const res: any = await delUserApplayProcess(deleteId.value)
    if (res.code === 200) {
      NotifyPlugin.success({ title: '提示：', content: '删除成功！' })
      getList()
    }
  } finally {
    deleteVisible.value = false
    dataLoading.value = false
  }
}
</script>
