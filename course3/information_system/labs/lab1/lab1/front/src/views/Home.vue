
<script setup>
import Header from "@/components/Header.vue";
import OrganizationTable from "@/components/OrganizationTable.vue";
</script>

<template>
  <body>
  <Header />
  <OrganizationTable />
  </body>
</template>

<style scoped>

html, body {
  height: 100%;
  margin: 0;
  padding: 0;
}

* {
  box-sizing: border-box;
}

body {
  background-color: rgb(232, 232, 232);
  font-family: 'Arial', sans-serif;
  color: #333;
  margin: 0;
  padding: 0;
}

</style>
