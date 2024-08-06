<template>
  <div class="app-container">
    <DesignList mode="Dev"></DesignList>
  </div>
</template>

<script>
import DesignList from './DesignList.vue'
export  default {
  name: 'devList',
  components: {DesignList}
}
</script>
