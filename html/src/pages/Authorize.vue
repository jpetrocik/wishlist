<template>
  <q-page>

  	<div id="page">

  		<div id="emailOption" v-bind:class="{mfaOption: mfaOption}">
  		<h2>Verification Required</h2>
  		<div id="sendInvite" v-bind:class="{ emailSent: emailSent}">
	  		<div>Please click on the link in the email invitation you received, to find the email search for subject "XMas Wish List Invitation".  If you can not find email invitation, enter your email address below and a new email invitation will be sent. To recieve a verification code via text message, <a @click.prevent="verificationOption" href="">click here</a></div>

	  		<div>
	  			<div><input id="email" placeholder="Enter Email" class="xmasInput" v-model="email" v-on:keyup.enter="sendInviteEmail"/></div>
	  			<div><input class="xmasButton"  @click="sendInviteEmail" type="button" value="Resend"/></div>
	  		</div>
  		</div>

  		<div id="checkEmail" v-bind:class="{ emailSent: emailSent}">
	  		An email has be sent to <span class="emailAddress">{{email}}</span> with a link to provide access.
  		</div>
  		</div>

  		<div id="mfaOption" v-bind:class="{mfaOption: mfaOption}">
  		<h2>Verification Required</h2>
  		<div id="sendMFA">
	  		<div>Enter your mobile number to receive a verification code.  When you receive the verification code, enter the code in the box below.</div>

	  		<div>
	  			<div><input id="phone"  placeholder="Enter Phone" class="xmasInput" v-model="phone" v-on:keyup.enter="requestVerificationCode"/></div>
	  			<div><input class="xmasButton"  @click="requestVerificationCode" type="button" value="Send"/></div>
	  		</div>

	  		<div>
	  			<div><input id="verificationCode"  placeholder="Enter Verification Code" class="xmasInput" v-model="verificationCode" v-on:keyup.enter="validateVerificationCode"/></div>
	  			<div><input class="xmasButton"  @click="validateVerificationCode" type="button" value="Verify"/></div>
	  		</div>

  		</div>
  		</div>

	</div>

  </q-page>
</template>

<style>
 @import '../assets/style.css';
 @import '../assets/wishlist.css';
</style>

<script>

export default {
  name: 'PageLogin',
  data(){
    return {
        email: null,
        emailSent: undefined,
        mfaOption: false,
        mfaSent: undefined,
        phone: undefined,
        verificationCode: undefined,
        verifictaionToken: undefined

    }
  },
  methods: {
    verificationOption(event) {
    	this.mfaOption = true
    },
    sendInviteEmail(event) {
    	let vue = this

	    this.$axios({
	        method: 'GET',
	        url: '/api/invitation/xmas/resend',
	        params: { 
	        	email: this.email,
	        	token: 'xmas'
	        }
	    }).then(function(response) {
	    	vue.emailSent = true
	    })
  	},
    requestVerificationCode(event) {
    	let vue = this

	    this.$axios({
	        method: 'POST',
	        url: '/api/mfa',
	        params: { 
	        	phone: this.phone
	        }
	    }).then(function(response) {
	    	vue.verifictaionToken = response.data
	    })
  	},
    validateVerificationCode(event) {
    	let vue = this

	    this.$axios({
	        method: 'GET',
	        url: '/api/mfa',
	        params: { 
	        	token: this.verifictaionToken,
	        	code: this.verificationCode
	        }
	    }).then(function(response) {
			vue.$router.push({ path: '/xmas' })
	    })
  	}

  }
 }
</script>
