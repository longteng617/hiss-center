<template>
  <div id="flow" style="padding:20px"></div>
</template>
<script>
import '/public/hiss.js'
import "@/components/HissDesign.css";
import {HISS_BASE_URL} from '@/constants/index'
export default {
  data() {
    return {
    }
  },
  mounted() {
    const createDesign = window.createDesign
    // 参数
    const url = window.location.href
    const params = {}
    const regex = /[?&]([^=#]+)=([^&#]*)/g
    let match
    while ((match = regex.exec(url))) {
      const key = decodeURIComponent(match[1])
      const value = decodeURIComponent(match[2])
      params[key] = value
    }
    // 路由
    const router = this.$router
    let currentRoutePath = ''
    const currentRouteName = router.currentRoute.name
    const currentRouteList = router.currentRoute.matched
    for (let i = 0; i < currentRouteList.length; i++) {
      if (currentRouteList[i].name === currentRouteName) {
        currentRoutePath = currentRouteList[i].path
      }
    }
    const mode = 'FormDataList'
    const meta = router.currentRoute.meta.params || {}
    const config = {
      routerName: mode,
      routerPath: currentRoutePath,
      routerMeta: {
        integration: true,
        id: meta.id || params.id,
        tenant: meta.appId || params.appId,
        currentRoutePath: currentRoutePath
      },
      serverUrl: HISS_BASE_URL,
      history: 'WEB'
    }
    let app = null
    app = createDesign(config)
    app.mount('#flow')
  }
}
</script>
