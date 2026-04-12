<template>
  <div class="profile-view">
    <section class="profile-panel">
      <div class="profile-heading">
        <span class="eyebrow">Profile</span>
        <h1>个人资料</h1>
        <p>账号：{{ authUser?.username || '普通用户' }}</p>
      </div>

      <form class="profile-form" @submit.prevent="saveProfile">
        <label class="field">
          <span>昵称</span>
          <input v-model.trim="profileForm.nickname" type="text" placeholder="请输入昵称">
        </label>

        <label class="field">
          <span>性别</span>
          <select v-model="profileForm.gender">
            <option value="">请选择</option>
            <option value="男">男</option>
            <option value="女">女</option>
            <option value="保密">保密</option>
          </select>
        </label>

        <label class="field avatar-field">
          <span>头像</span>
          <button class="avatar-upload-btn" type="button" @click="openAvatarPicker">设置头像</button>
          <input ref="avatarInput" type="file" accept="image/*" @change="handleAvatarChange">
        </label>

        <div class="avatar-preview" :class="{ empty: !profileForm.avatar }">
          <img v-if="profileForm.avatar" :src="profileForm.avatar" alt="头像预览">
          <span v-else>未设置头像</span>
        </div>

        <p v-if="message" class="form-message">{{ message }}</p>

        <div class="actions">
          <button class="ghost-btn" type="button" @click="$router.push('/')">返回首页</button>
          <button class="ghost-btn" type="button" @click="logout">退出登录</button>
          <button class="submit-btn" type="submit">保存资料</button>
        </div>
      </form>
    </section>
  </div>
</template>

<script>
export default {
  name: 'UserProfileView',
  data() {
    return {
      authUser: null,
      message: '',
      profileForm: {
        nickname: '',
        gender: '',
        avatar: ''
      }
    }
  },
  mounted() {
    this.loadProfile()
  },
  methods: {
    loadProfile() {
      try {
        this.authUser = JSON.parse(localStorage.getItem('luminaryAuth') || 'null')
      } catch (error) {
        this.authUser = null
      }

      if (!this.authUser || this.authUser.role !== 'user') {
        this.$router.push('/login')
        return
      }

      this.profileForm.nickname = this.authUser.nickname || ''
      this.profileForm.gender = this.authUser.gender || ''
      this.profileForm.avatar = this.authUser.avatar || ''
    },
    handleAvatarChange(event) {
      const file = event.target.files?.[0]
      if (!file) return

      const reader = new FileReader()
      reader.onload = () => {
        this.profileForm.avatar = reader.result
      }
      reader.readAsDataURL(file)
    },
    openAvatarPicker() {
      this.$refs.avatarInput?.click()
    },
    saveProfile() {
      if (!this.authUser) return

      const updatedAuth = {
        ...this.authUser,
        nickname: this.profileForm.nickname,
        gender: this.profileForm.gender,
        avatar: this.profileForm.avatar
      }

      localStorage.setItem('luminaryAuth', JSON.stringify(updatedAuth))
      this.updateStoredUser(updatedAuth)
      window.dispatchEvent(new Event('luminary-auth-change'))
      this.authUser = updatedAuth
      this.message = '资料已保存。'
    },
    updateStoredUser(updatedAuth) {
      try {
        const users = JSON.parse(localStorage.getItem('luminaryUsers') || '[]')
        if (!Array.isArray(users)) return

        const nextUsers = users.map(user => {
          if (user.username !== updatedAuth.username) return user

          return {
            ...user,
            nickname: updatedAuth.nickname,
            gender: updatedAuth.gender,
            avatar: updatedAuth.avatar
          }
        })

        localStorage.setItem('luminaryUsers', JSON.stringify(nextUsers))
      } catch (error) {
        localStorage.removeItem('luminaryUsers')
      }
    },
    logout() {
      localStorage.removeItem('luminaryAuth')
      window.dispatchEvent(new Event('luminary-auth-change'))
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.profile-view {
  min-height: calc(100vh - 220px);
  padding: 120px 24px 40px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.profile-panel {
  width: min(520px, 100%);
  padding: 34px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(100, 150, 255, 0.28);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(18px);
}

.profile-heading {
  text-align: center;
  margin-bottom: 26px;
}

.eyebrow {
  display: inline-flex;
  margin-bottom: 10px;
  color: #9bb8ff;
  font-size: 13px;
  letter-spacing: 0;
}

.profile-heading h1 {
  margin: 0 0 10px;
  color: #fff;
  font-size: 30px;
  font-weight: 700;
}

.profile-heading p {
  margin: 0;
  color: rgba(255, 255, 255, 0.68);
  font-size: 14px;
}

.profile-form {
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

.field input,
.field select,
.avatar-upload-btn {
  width: 100%;
  min-height: 46px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid rgba(100, 150, 255, 0.28);
  background: rgba(9, 12, 30, 0.62);
  color: #fff;
  outline: none;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.field select {
  appearance: none;
}

.field input:focus,
.field select:focus {
  border-color: #667eea;
  box-shadow: 0 0 18px rgba(102, 126, 234, 0.26);
}

.avatar-upload-btn {
  text-align: left;
  cursor: pointer;
}

.avatar-upload-btn:hover {
  border-color: #667eea;
  box-shadow: 0 0 18px rgba(102, 126, 234, 0.26);
}

.avatar-field input[type="file"] {
  display: none;
}

.avatar-preview {
  width: 98px;
  height: 98px;
  border-radius: 50%;
  align-self: center;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.25));
  color: rgba(255, 255, 255, 0.68);
  font-size: 13px;
}

.avatar-preview.empty {
  border-style: dashed;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-message {
  margin: 0;
  color: #a7f3d0;
  font-size: 14px;
  text-align: center;
}

.actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.ghost-btn,
.submit-btn {
  min-height: 48px;
  border-radius: 8px;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s ease;
}

.ghost-btn {
  border: 1px solid rgba(100, 150, 255, 0.28);
  background: rgba(255, 255, 255, 0.06);
}

.submit-btn {
  border: none;
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.3);
}

.ghost-btn:hover,
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(102, 126, 234, 0.34);
}

@media (max-width: 640px) {
  .profile-view {
    padding-top: 180px;
  }

  .profile-panel {
    padding: 24px;
  }

  .actions {
    grid-template-columns: 1fr;
  }
}
</style>
