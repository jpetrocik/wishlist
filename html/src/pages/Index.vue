<template>
  <q-page class="flex flex-center">
  <!--
        <div class="row">
        <div>Create A Wish List!</div>
        </div>
        <div class="row">
        <div>
        <input v-model="email" v-on:keyup.enter="onEmailEnter" type="text" id="email" name="email" placeholder="Enter Email Address"/>
        </div>
        </div>
   -->
  </q-page>
</template>

<style>
</style>

<script>

export default {
  name: 'PageIndex',
  data(){
    return {
      email: ''
    }
  },
  methods: {
    onEmailEnter(event) {
      let vue = this
      this.$axios({
        method: 'POST',
        url: '/api/start',
        params: {
          email: this.email
        }
      }).then(function(response) {
        vue.$axios({
          method: 'GET',
          url: '/api/invitation/' + response.data.token,
          params: {
            email: vue.email
          }
        }).then(function(response) {
          let nextPath = '/startRegistry/' + response.data.token
          vue.$router.push({ path: nextPath })
        })
      })
    }
  },
  created(){
     this.$router.push({ path: '/xmas' })
  }
}
</script>
