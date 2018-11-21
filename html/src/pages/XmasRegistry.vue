<template>
  <q-page>
  <div id="initials">
  <span v-for="(registry, index) in registries" :key="registry.token" @click="switchRegistry(index)"> {{ registry.name }}</span>
  </div>

 <div id="page">
<template v-if="activeRegistry">
	<div id="header">{{activeRegistry.name}} Christmas Wishlist</div>
	<div class="note">* Green indicates gift suggestion.</div>
	
  	<div id="wishlist">
	  <div  v-for="(g, index) in gifts" :key="g.id" ref="'gift-'+index" class="gift" v-bind:class="{ purchased: g.purchased, secret: g.secret, editing:g.id == selected}">
		  	<div class="purchasedCheckBox" v-bind:class="{ purchased: g.purchased }" @click="purchaseGift(g.id, index)"></div>
		  	<div class="giftDetails">
		  	<span class="giftDescr" @click="selected = g.id">{{ g.descr }}</span>
		  	<span class="giftPurchasedBy">({{ g.purchasedBy }})</span> <a v-if="g.url" class="giftLink" target="_blank" v-bind:href="g.url"><i class="fas fa-external-link-alt"></i></a>
		  	</div>
		  	<div class="giftEdit">
		  	<div><textarea class="giftEditDescr xmasInput" v-model="g.descr"></textarea></div>
		  	<div><input class="giftEditUrl xmasInput" v-model="g.url"/></div>
		  	<div><input type="button" class="xmasButton" value="Save" @click="selected = undefined;updateGift(g)"/><input class="cancel" type="button" @click="selected = undefined" value="Cancel"></div>
		  	</div>
  	  </div>
  	</div>
		

	<div id="addGift">
		<div>
		<img src="../assets/images/gifts.gif"/>
			<h2>Add Gift</h2>
			Use the form below to add gift ideas to the gift idea list. Paste a link to the gift idea you would like, otherwise you can just add a decsription of your gift idea. 
		</div>
		<div style="clear:both"></div>
		<a anchor="edit"></a>
		<div><input type="text" class="xmasInput" v-model="addGiftUrl" @paste="downloadMetadata" placeholder="Paste Link"></div>
		<div><textarea id="addGiftDescr" class="xmasInput" placeholder="Enter Description" v-model="addGiftDescr" v-bind:class="{ loading: loading}"></textarea></div>
		<div><input type="submit" @click="addGift" class="xmasButton" value="Add"></div>
	</div>
</template>
</div>

  </q-page>
</template>

<style>
 @import '../assets/style.css';
 @import '../assets/wishlist.css';
</style>

<script>

export default {
  name: 'PageStartRegistry',
  data(){
    return {
        registries: [],
        activeRegistry: undefined,
        gifts: [],
        addGiftUrl: null,
        addGiftDescr: null,
        selected: undefined,
        loading: undefined
    }
  },
  methods: {
    switchRegistry(index, event) {
    	this.activeRegistry = this.registries[index]
    	this.gifts = []
     	this.addGiftUrl = null
	    this.addGiftDescr = null

    	this.loadRegistry()
    },
  	downloadMetadata(event) {
        let vue = this

        let giftUrl = event.clipboardData.getData('text/plain')
        if (giftUrl === '') {
        	return
        }

		this.loading = true

  	    this.$axios({
          method: 'POST',
          url: '/api/url/',
          params: { 
          	pageUrl: giftUrl 
          }
        }).then(function(response) {
		  vue.loading = false
          vue.addGiftDescr = response.data.title
        })
  	},
  	loadRegistry() {
        let vue = this

	    this.$axios({
	        method: 'GET',
	        url: '/api/registry/' + vue.activeRegistry.id + '/item'
	    }).then(function(response) {
	     vue.gifts = response.data
	    })
  	},
  	updateGift(gift) {
        let vue = this

	    this.$axios({
	        method: 'PUT',
	        url: '/api/registry/' + vue.activeRegistry.id + '/item/' + gift.id,
	        data: gift

	    }).then(function(response) {
	    })
	},
  	addGift() {
        let vue = this

        if (vue.addGiftDescr == null || vue.addGiftDescr.length === 0) {
			return        
        }

	    this.$axios({
	        method: 'POST',
	        url: '/api/registry/' + vue.activeRegistry.id + '/item',
	        data: {
	          descr: vue.addGiftDescr,
	          url: vue.addGiftUrl	
	        }

	    }).then(function(response) {
	     	vue.gifts.push(response.data)
	     	vue.addGiftUrl = null
	     	vue.addGiftDescr = null
	    })
	},
  	purchaseGift(giftId, index, event) {
        let vue = this

	    this.$axios({
	        method: 'PUT',
	        url: '/api/registry/' + vue.activeRegistry.id + '/item/' + giftId + '/purchased'
	    }).then(function(response) {
	    	vue.gifts[index].purchased = !vue.gifts[index].purchased
	    })
	}

  },
  beforeMount(){
        let vue = this

        //load the group registries
        this.$axios({
          method: 'GET',
          url: '/api/group/' + this.$route.params.token,
          params: this.$route.query
        }).then(function(response) {
            vue.registries = response.data
	        vue.$axios({
	          method: 'GET',
	          url: '/api/group/' + vue.$route.params.token + '/default'
	        }).then(function(response) {
	            vue.activeRegistry = response.data
    	        vue.loadRegistry()
	        })
        }).catch(function(error){
           	if (error.response.status === 403) {
				vue.$router.push({ path: '/authorize' })
			}
        })
  }
}
</script>
