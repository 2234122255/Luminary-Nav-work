<template>
  <div class="login-view">
    <section class="login-panel">
      <div class="login-heading">
        <h1>Sign In</h1>
        <p v-if="banner" class="banner">{{ banner }}</p>
      </div>

      <div class="login-tabs">
        <button :class="{ active: activeTab === 'user' }" type="button" @click="setActiveTab('user')">
          User
        </button>
        <button :class="{ active: activeTab === 'admin' }" type="button" @click="setActiveTab('admin')">
          Admin
        </button>
      </div>

      <form v-if="activeTab === 'user'" class="login-form" @submit.prevent="loginAsUser">
        <label class="field">
          <span>Username</span>
          <input v-model.trim="userForm.username" type="text" autocomplete="username" placeholder="Enter username" />
        </label>

        <label class="field">
          <span>Password</span>
          <input v-model="userForm.password" type="password" autocomplete="current-password" placeholder="Enter password" />
        </label>

        <div class="aux-links">
          <button class="aux-link" type="button">忘记密码</button>
          <router-link class="aux-link" to="/register">立即注册</router-link>
        </div>

        <p v-if="message" class="form-message">{{ message }}</p>
        <button class="submit-btn" type="submit">Sign In</button>
      </form>

      <form v-else class="login-form" @submit.prevent="loginAsAdmin">
        <label class="field">
          <span>Admin Username</span>
          <input v-model.trim="adminForm.username" type="text" autocomplete="username" placeholder="administrator" />
        </label>

        <label class="field">
          <span>Admin Password</span>
          <input v-model="adminForm.password" type="password" autocomplete="current-password" placeholder="Enter password" />
        </label>

        <p v-if="message" class="form-message">{{ message }}</p>
        <button class="submit-btn" type="submit">Enter Admin</button>
      </form>
    </section>
  </div>
</template>

<script>
const ADMIN_USERNAME = 'administrator'
const ADMIN_PASSWORD = '123456'

export default {
  name: 'LoginView',
  data() {
    return {
      activeTab: 'user',
      message: '',
      userForm: {
        username: '',
        password: ''
      },
      adminForm: {
        username: ADMIN_USERNAME,
        password: ''
      }
    }
  },
  computed: {
    banner() {
      const registered = this.$route?.query?.registered
      if (!registered) return ''
      return 'Registration complete. Please sign in.'
    }
  },
  methods: {
    saveAuth(auth) {
      localStorage.setItem('luminaryAuth', JSON.stringify(auth))
      window.dispatchEvent(new Event('luminary-auth-change'))
    },
    setActiveTab(tab) {
      this.activeTab = tab
      this.message = ''
    },
    loginAsUser() {
      this.message = ''
      if (!this.userForm.username || !this.userForm.password) {
        this.message = 'Please enter username and password.'
        return
      }

      // No backend: accept any non-empty input and mark as signed in.
      this.saveAuth({
        role: 'user',
        username: this.userForm.username
      })
      this.$router.push('/')
    },
    loginAsAdmin() {
      this.message = ''
      if (this.adminForm.username !== ADMIN_USERNAME || this.adminForm.password !== ADMIN_PASSWORD) {
        this.message = 'Invalid admin credentials.'
        return
      }

      this.saveAuth({
        role: 'admin',
        username: ADMIN_USERNAME
      })
      this.$router.push('/admin')
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

.banner {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.78);
  font-size: 14px;
}

.login-tabs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 24px;
}

.login-tabs button {
  padding: 12px 16px;
  border: 1px solid rgba(100, 150, 255, 0.25);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  transition: all 0.25s ease;
}

.login-tabs button.active,
.login-tabs button:hover {
  color: #fff;
  border-color: rgba(102, 126, 234, 0.8);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.42), rgba(118, 75, 162, 0.38));
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

.aux-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -10px;
}

.aux-link {
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  text-decoration: none;
}

.aux-link:hover {
  color: #fff;
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

@media (max-width: 640px) {
  .login-view {
    padding-top: 180px;
  }

  .login-panel {
    padding: 24px;
  }
}
</style>
