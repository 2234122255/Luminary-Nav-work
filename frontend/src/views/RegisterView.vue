<template>
  <div class="login-view">
    <section class="login-panel">
      <div class="login-heading">
        <h1>Create Account</h1>
        <p class="subtitle">No backend storage. Submit redirects to Sign In.</p>
      </div>

      <form class="login-form" @submit.prevent="submit">
        <label class="field">
          <span>Username</span>
          <input v-model.trim="form.username" type="text" autocomplete="username" placeholder="Choose a username" />
        </label>

        <label class="field">
          <span>Password</span>
          <input v-model="form.password" type="password" autocomplete="new-password" placeholder="Create a password" />
        </label>

        <label class="field">
          <span>Confirm Password</span>
          <input v-model="form.confirmPassword" type="password" autocomplete="new-password" placeholder="Re-enter password" />
        </label>

        <p v-if="message" class="form-message">{{ message }}</p>

        <button class="submit-btn" type="submit">Register</button>
        <router-link class="register-link" to="/login">Back to Sign In</router-link>
      </form>
    </section>
  </div>
</template>

<script>
export default {
  name: 'RegisterView',
  data() {
    return {
      message: '',
      form: {
        username: '',
        password: '',
        confirmPassword: ''
      }
    }
  },
  methods: {
    submit() {
      this.message = ''
      if (!this.form.username || !this.form.password || !this.form.confirmPassword) {
        this.message = 'Please fill in all fields.'
        return
      }
      if (this.form.password !== this.form.confirmPassword) {
        this.message = 'Passwords do not match.'
        return
      }

      // No persistence by design; just redirect to login.
      this.$router.push({ name: 'Login', query: { registered: '1' } })
    }
  }
}
</script>

<style scoped>
.login-view {
  min-height: calc(100vh - 220px);
  padding: 120px 24px 40px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.login-panel {
  width: min(520px, 100%);
  padding: 34px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(100, 150, 255, 0.28);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(18px);
}

.login-heading {
  text-align: center;
  margin-bottom: 26px;
}

.login-heading h1 {
  margin: 0;
  color: #fff;
  font-size: 30px;
  font-weight: 700;
}

.subtitle {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 14px;
}

.field input {
  width: 100%;
  min-height: 46px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid rgba(100, 150, 255, 0.28);
  background: rgba(9, 12, 30, 0.62);
  color: #fff;
  outline: none;
  transition: all 0.25s ease;
}

.field input:focus {
  border-color: #667eea;
  box-shadow: 0 0 18px rgba(102, 126, 234, 0.26);
}

.field input::placeholder {
  color: rgba(255, 255, 255, 0.42);
}

.form-message {
  margin: 0;
  color: #fca5a5;
  font-size: 14px;
  text-align: center;
}

.submit-btn {
  min-height: 48px;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.3);
  transition: all 0.25s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(102, 126, 234, 0.4);
}

.register-link {
  align-self: center;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  text-decoration: none;
}

.register-link:hover {
  color: #fff;
}

@media (max-width: 640px) {
  .login-view {
    padding-top: 180px;
  }

  .login-panel {
    padding: 24px;
  }
}
</style>

