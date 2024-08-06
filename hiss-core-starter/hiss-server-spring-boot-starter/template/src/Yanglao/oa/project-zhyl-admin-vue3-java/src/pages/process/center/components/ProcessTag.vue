<template>
  <t-card
    bordered
    hover-shadow
    v-if="processList.length > 0"
    v-for="item in processList"
    :key="item.id"
    class="card"
    @click="startProcess(item)"
  >
    <div class="img">
      <img v-if="item.icon" :src="item.icon" />
      <t-icon v-else name="chart-bubble" class="hiss-default-icon" />
    </div>
    <div class="name">{{ item.name }}</div>
    <div class="design" v-if="!item.deploymentId">设计中</div>
    <div class="deployment" v-else>已发布</div>
  </t-card>
  <div class="empty" v-else>暂无内容哦～</div>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {getCategoryProcessList, startBisProcess} from "@/api/process";
import {MessagePlugin} from "tdesign-vue-next";
import {useRouter} from "vue-router";
const props = defineProps({
  category: {
    type: String,
    default: ''
  }
})

// ------定义变量------
const processList = ref([]) // 列表数据
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
    const res: any = await getCategoryProcessList(props.category) // 获取列表数据
    processList.value = res.data
  } finally {
    dataLoading.value = false
  }
}
// 发起流程
const router = useRouter()
const startProcess = async (row) => {
  console.log(row)
  if (row.deploymentId) {
    dataLoading.value = false
    try {
      const res: any = await startBisProcess(row.id) // 获取列表数据
      const temp = res.msg
      if (temp) {
        const params = { id: temp }
        router.push({ path: 'handleBis', query: params })
      }
    } finally {
      dataLoading.value = false
    }
  } else {
    MessagePlugin.error('流程正在设计中，暂时不能发起')
  }
}
</script>
<style lang="less" scoped>
.card {
  width: 300px;
  line-height: 40px;
  float: left;
  margin: 5px;
  border: 1px solid #f0f0f0;
  .img {
    float: left;
    width: 28px;
    height: 28px;
  }
  .name {
    float: left;
    overflow: hidden;
    margin-left: 15px;
    width: 70%;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
.card:hover {
  cursor: pointer;
}
.empty {
  color: #d0d0d0;
}
.design {
  float: right;
  font-size: 8px;
  color: #ffffff;
  margin-top: -16px;
  margin-right: -25px;
  background-color: #ffd448;
  height: 15px;
  padding: 2px 5px;
  line-height: 10px;
  position: relative;
  border-bottom-left-radius: 5px;
}
.deployment {
  float: right;
  font-size: 8px;
  color: #ffffff;
  margin-top: -16px;
  margin-right: -25px;
  background-color: #00bfa7;
  height: 15px;
  padding: 2px 5px;
  line-height: 10px;
  position: relative;
  border-bottom-left-radius: 5px;
}

.hiss-default-icon {
  width: 40px;
  height: 40px;
  background-color: #00b799;
  color: #ffffff;
  padding: 5px;
  border-radius: 5px;
  font-weight: bold;
}
</style>
