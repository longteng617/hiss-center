<!-- 基础表格组件 -->
<template>
  <div class="baseList">
    <div class="tableBoxs">
      <t-table
        :data="listData"
        :columns="COLUMNS"
        :row-key="rowKey"
        vertical-align="middle"
        :hover="true"
        :pagination="pagination.total > 10 ? pagination : null"
        :disable-data-page="total <= 10"
        :loading="dataLoading"
        table-layout="fixed"
        table-content-width="100%"
        @page-change="onPageChange"
      >
        <!-- 序号 -->
        <template #rowIndex="{ rowIndex }">{{ rowIndex + 1 }}</template>
        <!-- end -->
        <template #type="{ row }">
          <span v-if="row.status === 1">退住</span>
          <span v-else-if="row.status === 2">请假</span>
          <span v-else>入住</span>
        </template>
        <!-- 完成时间-->
        <template #finishTime="{ row }">
          {{ row.finishTime ? row.finishTime : '--' }}
        </template>
        <!-- end -->
        <!-- 在操作栏添加删除、编辑、查看三种操作 -->
        <template #op="{ row }">
          <div class="operateCon">
            <a class="font-bt" @click="toHandleProcess(row.id)">{{ opName }}</a>
          </div>
        </template>
        <!-- end -->
        <!-- 暂无数据 -->
        <template #empty>
          <NoData></NoData>
        </template>
        <!-- end -->
      </t-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { watch, ref } from 'vue'
import { useRouter } from 'vue-router'
import { COLUMNS } from '../constants' // 表格列
// 组件
import NoData from '@/components/noData/index.vue'

// 获取父级数据
const props = defineProps({
  // 列表数据
  listData: {
    type: Object,
    default: () => {
      return []
    }
  },
  opName: {
    type: String,
    default: '查看'
  },
  // 总条数
  total: {
    type: Number,
    default: 0
  },
  // 分页
  pagination: {
    type: Object,
    default: () => ({})
  },
  // 加载状态
  dataLoading: {
    type: Boolean,
    default: false
  }
})
// 监听父级传过来的数据
watch(props, (val) => {
  pagination.value = {
    ...val.pagination,
    total: val.total
  }
})
// --------------
// 路由
const router = useRouter()
const toHandleProcess = (id) => {
  const params = { id }
  router.push({ path: 'handleBis', query: params })
}
// ------定义变量------
const pagination = ref({}) // 分页需要加上total
// 行的key
const rowKey = 'index'
const emit = defineEmits(['handleOpen', 'getCurrent'])
// 点击翻页
const onPageChange = (pageInfo) => {
  emit('getCurrent', pageInfo)
}
</script>
