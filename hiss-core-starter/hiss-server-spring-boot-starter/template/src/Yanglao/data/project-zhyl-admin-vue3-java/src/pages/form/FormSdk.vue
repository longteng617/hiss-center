<template>
  <div id="flow"></div>
</template>
<script setup>
import '/public/hiss.js'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { HISS_BASE_URL } from '@/constants'

const createDesign = window.createDesign

// 路由
const router = useRouter()
const currentRoutePath = ref('')
const currentRouteName = router.currentRoute.value.name
const currentRouteList = router.currentRoute.value.matched
for (let i = 0; i < currentRouteList.length; i++) {
  if (currentRouteList[i].name === currentRouteName) {
    currentRoutePath.value = currentRouteList[i].path
  }
}
const mode = ref('FormDataList')
const meta = router.currentRoute.value.meta.params
const config = {
  routerName: mode.value,
  routerPath: currentRoutePath.value,
  routerMeta: {
    integration: true,
    id: meta.id,
    tenant: meta.appId,
    currentRoutePath: currentRoutePath.value
  },
  serverUrl: HISS_BASE_URL
}
let app = null
onMounted(() => {
  app = createDesign(config)
  app.mount('#flow')
})
</script>
<style>
.top .topBox .t-layout__sider .czri-sidebar-layout .czri-side-nav {
  position: relative;
}
.top .topBox .t-layout__sider .t-menu__item {
  color: #ffffff;
}
.top .topBox .t-layout__sider .t-is-active {
  color: #333333;
}
.top .topBox .t-layout__sider {
  margin-top: 40px;
}
.t-layout__sider .czri-sidebar-layout .czri-side-nav {
  width: 232px !important;
}
.top .topBox .czri-sidebar-layout .czri-side-nav .t-menu__operations {
  margin-bottom: 15px;
}
.hp-handler-body .hp-handler-header {
  padding: 0px 0px;
  --el-header-padding: 0px 0px;
}
.hp-handler-header .el-card__body {
  padding: 20px 0px;
}
#pane-flow,
#pane-form {
  height: 80vh;
}
.hiss-handler-button {
  color: #ffffff;
  border: none;
  background: #00b799;
}
</style>
