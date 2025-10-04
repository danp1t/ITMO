<template>
  <router-view/>
</template>

<script>
import Home from "@/views/Home.vue";

export default {
  components: {Home}
};
</script>

<style>
/* Общие стили для адаптивности */
@media (min-width: 1137px) {
  /* Десктопный режим */
}

@media (min-width: 889px) and (max-width: 1136px) {
  /* Планшетный режим */
}

@media (max-width: 888px) {
  /* Мобильный режим */
}
</style>
