<!-- 预约等级列表页 -->
<template>
  <div class="min-h subscribe bg-wt">
    <div class="group" v-for="item in processCategoryList" :key="item.id">
      <div class="hiss-title">
        <t-icon name="layers"></t-icon> {{ item.name }}
      </div>
      <div class="hiss-body">
        <ProcessTag :category="item.id" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {getProcessCategoryList} from "@/api/process";
import ProcessTag from './components/ProcessTag.vue'
// ------定义变量------
const processCategoryList = ref([]) // 列表数据
const dataLoading = ref(false) // 加载中

// 生命周期
onMounted(() => {
  getList()
})
// ------定义方法------
// 获取列表数据
const getList = async () => {
  dataLoading.value = false
  try {
    const res: any = await getProcessCategoryList({}) // 获取列表数据
    processCategoryList.value = res.data
  } finally {
    dataLoading.value = false
  }
}
</script>
<style lang="less">
.bg-wt {
  background-color: #fafafa;
}
.group {
  width: 100%;
  line-height: 20px;
  font-size: 12px;
  .hiss-title{
    text-align: left;
    float: left;
    width: 100%;
  }
  .hiss-body {
    padding: 15px;
  }
  margin-bottom: 20px;
  overflow: hidden;
}
</style>
