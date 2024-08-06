<template>
  <div>
    <el-card
      bordered
      hover-shadow
      v-if="processList.length > 0"
      v-for="item in processList"
      :key="item.id"
      class="hiss-card"
    >
      <div class="img" @click="startProcess(item)">
        <img v-if="item.icon" :src="item.icon" />
        <svg-icon v-else icon-class="swagger" class="hiss-default-icon" />
      </div>
      <div class="name" @click="startProcess(item)">{{ item.name }}</div>
      <div class="design" v-if="!item.deploymentId">设计中</div>
      <div class="deployment" v-else>已发布</div>
    </el-card>
    <div class="hiss-empty" v-if="processList.length == 0">暂无内容哦～</div>
  </div>
</template>
<script>
import {getCategoryProcessList, startBisProcess} from "@/api/process";
export default {
  name: "Login",
  props : {
    category: {
      type: Number,
      default: ''
    }
  },
  data() {
    return {
      processList: []
    }
  },
  mounted() {
    this.getList()
  },
  methods : {
    async getList() {
      const res = await getCategoryProcessList(this.category) // 获取列表数据
      this.processList = res.data
    },
    async startProcess(row) {
      console.log(row)
      if (row.deploymentId) {
        const res = await startBisProcess(row.id) // 获取列表数据
        const temp = res.msg
        if (temp) {
          const params = {id: temp}
          this.$router.push({path: 'handleBis', query: params})
        }
      } else {
        this.$modal.msgError("流程正在设计中，暂时不能发起");
      }
    }
  }
}
</script>

<style scoped>
.hiss-card {
  width: 300px;
  line-height: 40px;
  float: left;
  margin: 10px 5px 0px;
  padding-bottom: 18px;
}
.hiss-card .img {
  float: left;
  width: 28px;
  height: 28px;
  margin-right: 15px;
}
.hiss-card .name {
  float: left;
  font-size: 13px;
  margin-left: 15px;
  line-height: 40px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  width: 175px;
}
.hiss-card:hover {
  cursor: pointer;
}
.hiss-empty {
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
  width: 42px;
  height: 42px;
  color: #FFFFFF;
  font-weight: bold;
  border: 1px crimson solid;
  background-color: crimson;
  border-radius: 5px;
  padding: 12px;
}
</style>
