<!-- 预约等级列表页 -->
<template>
  <div class="min-h subscribe bg-wt" style="padding: 30px">
    <div class="group" v-for="item in processCategoryList" :key="item.id">
      <div class="hiss-title">
        <el-tag effect="plain" round> {{ item.name }}</el-tag>
      </div>
      <div class="hiss-body">
        <ProcessTag :category="item.id" />
      </div>
    </div>
  </div>
</template>
<script>
import {getProcessCategoryList} from "@/api/process";
import ProcessTag from './components/ProcessTag.vue'
export default {
  name: "Login",
  components: {ProcessTag },
  data() {
    return {
      processCategoryList: []
    }
  },
  mounted() {
    this.getList()
  },
  methods : {
    async getList() {
      const res = await getProcessCategoryList({}) // 获取列表数据
      this.processCategoryList = res.data
    }
  }
}
</script>

<style>
.bg-wt {
  background-color: #fafafa;
}
.group {
  width: 100%;
  line-height: 20px;
  font-size: 12px;
  margin-bottom: 20px;
  overflow: hidden;
}
.group .hiss-title{
  text-align: left;
  float: left;
  width: 100%;
}
.group .hiss-body {
  padding: 15px;
}
</style>
