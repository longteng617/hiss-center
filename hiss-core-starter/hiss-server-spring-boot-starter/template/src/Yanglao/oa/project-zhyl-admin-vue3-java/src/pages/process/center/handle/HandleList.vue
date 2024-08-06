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
    <!-- 表格 -->
    <TableList
      :list-data="listData"
      :pagination="pagination"
      :total="total"
      :op-name="opName"
      @get-current="getCurrent"
    ></TableList>
    <!-- end -->
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref, watch} from 'vue'
// 表格列表
import TableList from './components/TableList.vue'
// 搜索框表单
import SearchFormBox from './components/SearchForm.vue'
import {getUserHandleProcessList} from '@/api/process'
// ------定义变量------
const listData = ref([]) // 列表数据
const dataLoading = ref(false) // 加载中
const total = ref(0) // 总条数
const props = defineProps({
  status: {
    type: String,
    default: 'ACTIVE'
  },
  opName: {
    type: String,
    default: '查看'
  }
})

const visible = ref(false) // 申请类别弹层
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
    pagination.value.status = props.status
    const res: any = await getUserHandleProcessList(pagination.value) // 获取列表数据
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
// 翻页设置当前页
const getCurrent = (val) => {
  pagination.value.pageNum = val.current
  pagination.value.pageSize = val.pageSize
  getList()
}
// 发起申请
const handleApply = () => {
  visible.value = true
}
// 关闭发起申请弹层
const handleApplyClose = () => {
  visible.value = false
}
</script>
