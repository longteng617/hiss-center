<template>
  <div class="app-container">
    <HandleList status="ACTIVE" opName="办理"></HandleList>
  </div>
</template>

<script>
import HandleList from './HandleList.vue'
export  default {
  name: 'Active',
  components: {HandleList}
}
</script>
