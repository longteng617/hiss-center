<!-- 搜索框区域 -->
<template>
  <div class="formBox">
    <t-form ref="form" :model="searchData" :label-width="98">
      <t-row>
        <t-col>
          <t-form-item label="流程名：" name="name">
            <t-input
              v-model="searchData.name"
              class="form-item-content"
              type="search"
              placeholder="请输入"
              clearable
            />
          </t-form-item>
        </t-col>
        <t-col>
          <t-form-item label="业务号：" name="businessKey">
            <t-input
              v-model="searchData.businessKey"
              class="form-item-content"
              type="search"
              placeholder="请输入"
              clearable
            />
          </t-form-item>
        </t-col>
        <!-- 按钮区域 -->
        <t-col class="searchBtn">
          <button class="bt-grey wt-60" @click="handleReset()">重置</button>
          <button class="bt wt-60" @click="handleSearch()">搜索</button>
        </t-col>
      </t-row>
    </t-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { invoicesType } from '@/utils/commonData'
// 获取父组件值、方法
const props = defineProps({
  // 搜索对象
  searchData: {
    type: Object,
    default: () => ({})
  }
})
// 触发父组件的方法
const emit: Function = defineEmits(['handleSearch', 'handleReset'])
const form = ref(null)
// 重置表单
const handleReset = () => {
  form.value.reset()
  emit('handleReset')
}
// 搜索表单
const handleSearch = () => {
  emit('handleSearch')
}
</script>
