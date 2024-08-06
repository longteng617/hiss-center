<template>
  <div id="flow"></div>
</template>
<script>
import "@/components/HissDesign.css";
import '/public/hiss.js'
import { HISS_BASE_URL } from '@/constants/index'
export default {
  name:'HissDesign',
  props:{
    name : {
      type: String,
      default: null
    }
  },
  mounted() {
    this.initData()
  },
  methods:{
    initData(){
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
      const config = {
        routerName: this.name,
        routerPath: currentRoutePath,
        routerMeta: {
          integration: true,
          id: params.id,
          currentRoutePath: currentRoutePath
        },
        history: 'Web',
        serverUrl: HISS_BASE_URL
      }
      let app = createDesign(config)
      app.mount('#flow')
    }
  }
}
</script>
