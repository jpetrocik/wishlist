<template>
  <q-page class="flex flex-center">
  <div>{{registry.name}} Wish List</div>
  </q-page>
</template>

<style>
</style>

<script>

export default {
  name: 'PageStartRegistry',
  data(){
    return {
        registry: null,
        items: null
    }
  },
  methods: {

  },
  created(){
  },
  beforeCreate(){
        let vue = this
        this.$axios({
          method: 'GET',
          url: '/api/invitation/' + this.$route.params.token
        }).then(function(response) {
            vue.registry = response.data

            vue.$axios({
                method: 'GET',
                url: '/api/registry/' + response.data.id + '/item'
            }).then(function(response) {
             vue.items = response.data
            })

        })
  }
}
</script>
