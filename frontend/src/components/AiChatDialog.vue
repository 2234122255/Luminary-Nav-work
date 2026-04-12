<template>
  <div v-if="visible" class="ai-chat-overlay" :class="{ minimized: isMinimized }">
    <div class="ai-chat-window" :style="{ width: windowWidth + 'px', height: windowHeight + 'px' }" ref="chatWindow">
      <!-- 拖拽缩放手柄 -->
      <div class="resize-handle" @mousedown="initResize"></div>
      <!-- 头部 -->
      <div class="chat-header">
        <div class="header-title">
          <span class="bot-icon">🤖</span>
          <h3>AI 学术助手</h3>
        </div>
        <div class="header-actions">
          <button @click="toggleMinimize" class="action-btn" title="最小化">
            {{ isMinimized ? '□' : '—' }}
          </button>
          <button @click="$emit('close')" class="action-btn" title="关闭">✕</button>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="chat-body" v-show="!isMinimized" ref="chatBody">
        <div class="message-list">
          <div 
            v-for="(msg, index) in messages" 
            :key="index"
            class="message-wrapper"
            :class="msg.role"
          >
            <div class="message-content">
              <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
              <div class="bubble">
                <div class="text" v-html="formatText(msg.content)"></div>
                <!-- 渲染名片 -->
                <div v-if="msg.scholarCard" class="scholar-card-wrapper">
                  <div class="scholar-card" :id="'scholar-card-' + index">
                    <div class="card-header">
                      <div class="card-avatar">{{ msg.scholarCard.name.charAt(0) }}</div>
                      <div class="card-info">
                        <h4>{{ msg.scholarCard.name }}</h4>
                        <p class="card-title">{{ msg.scholarCard.title }}</p>
                        <p class="card-org">
                          <span class="icon">🏛️</span> {{ msg.scholarCard.org }}
                        </p>
                      </div>
                    </div>

                    <div class="card-body">
                      <div class="info-row">
                        <span class="info-label">研究方向:</span>
                        <span class="info-value">{{ msg.scholarCard.research }}</span>
                      </div>
                      <div class="info-row">
                        <span class="info-label">主要合作者:</span>
                        <span class="info-value">{{ msg.scholarCard.collaborators }}</span>
                      </div>
                    </div>

                    <div class="card-stats">
                      <div class="stat-item">
                        <span class="stat-label">论文总数</span>
                        <span class="stat-value">{{ msg.scholarCard.paperCount }}</span>
                      </div>
                      <div class="stat-item divider"></div>
                      <div class="stat-item">
                        <span class="stat-label">学术影响力</span>
                        <span class="stat-value">{{ formatScore(msg.scholarCard.score) }}</span>
                      </div>
                    </div>
                    
                    <div class="card-footer">
                      <span class="logo-text">Luminary Nav</span> 学者专属名片
                    </div>
                  </div>
                  <button class="export-btn" @click="exportCard(index, msg.scholarCard.name)">
                    <span class="icon">📥</span> 导出名片
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div v-if="loading" class="message-wrapper assistant">
            <div class="message-content">
              <div class="avatar">🤖</div>
              <div class="bubble typing">
                <span>.</span><span>.</span><span>.</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-footer" v-show="!isMinimized">
        <div class="input-container">
          <textarea 
            v-model="inputMessage" 
            @keydown.enter.prevent="sendMessage"
            placeholder="询问关于学者、合著网络的问题... 例如: 生成张三的名片"
            rows="1"
            ref="chatInput"
          ></textarea>
          <button class="send-btn" @click="sendMessage" :disabled="loading || !inputMessage.trim()">
            发送
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'AiChatDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isMinimized: false,
      windowWidth: 380,
      windowHeight: 600,
      isResizing: false,
      startY: 0,
      startX: 0,
      startWidth: 0,
      startHeight: 0,
      inputMessage: '',
      messages: [
        {
          role: 'assistant',
          content: '您好！我是Luminary Nav的AI学术助手。我可以为您分析学者的合著网络、研究方向，或者为您生成学者名片。请问有什么我可以帮您的？\n（您可以回复"生成名片 [学者名]"来体验名片功能）'
        }
      ],
      loading: false
    }
  },
  watch: {
    visible(val) {
      if (val && !this.isMinimized) {
        this.$nextTick(() => {
          if (this.$refs.chatInput) this.$refs.chatInput.focus();
        });
      }
    }
  },
  methods: {
    toggleMinimize() {
      this.isMinimized = !this.isMinimized;
    },
    initResize(e) {
      this.isResizing = true;
      this.startX = e.clientX;
      this.startY = e.clientY;
      this.startWidth = this.windowWidth;
      this.startHeight = this.windowHeight;
      
      document.documentElement.addEventListener('mousemove', this.doResize);
      document.documentElement.addEventListener('mouseup', this.stopResize);
      
      // 防止拖拽时选中文字
      e.preventDefault();
    },
    doResize(e) {
      if (!this.isResizing || this.isMinimized) return;
      
      // 注意：由于窗口固定在右下角，拖拽左上角时，向左移动X减小(宽度增加)，向上移动Y减小(高度增加)
      const newWidth = this.startWidth + (this.startX - e.clientX);
      const newHeight = this.startHeight + (this.startY - e.clientY);
      
      // 设置最小和最大宽高限制
      this.windowWidth = Math.max(300, Math.min(newWidth, window.innerWidth - 40));
      this.windowHeight = Math.max(400, Math.min(newHeight, window.innerHeight - 100));
    },
    stopResize() {
      this.isResizing = false;
      document.documentElement.removeEventListener('mousemove', this.doResize);
      document.documentElement.removeEventListener('mouseup', this.stopResize);
    },
    formatText(text) {
      return text.replace(/\n/g, '<br>');
    },
    formatScore(score) {
      return score ? Number(score).toFixed(2) : '0.00';
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const body = this.$refs.chatBody;
        if (body) {
          body.scrollTop = body.scrollHeight;
        }
      });
    },
    async sendMessage() {
      if (!this.inputMessage.trim() || this.loading) return;

      const userMsg = this.inputMessage.trim();
      this.inputMessage = '';
      
      this.messages.push({ role: 'user', content: userMsg });
      this.scrollToBottom();
      
      this.loading = true;

      try {
        // 构建请求给后端百炼代理接口
        const apiMessages = this.messages
          .filter(m => !m.scholarCard) // 过滤掉包含UI组件的
          .map(m => ({ role: m.role, content: m.content }));

        const response = await axios.post('/api/chat', {
          messages: apiMessages
        });

        let aiContent = '抱歉，我现在无法回答。';
        if (response.data && response.data.choices && response.data.choices.length > 0) {
          aiContent = response.data.choices[0].message.content;
        } else if (typeof response.data === 'string') {
            try {
                let parsed = JSON.parse(response.data);
                if (parsed.choices && parsed.choices.length > 0) {
                    aiContent = parsed.choices[0].message.content;
                }
            } catch(e) {
                aiContent = response.data; // 可能是直接的文本或格式化错误的JSON
            }
        }
        
        // 检查是否返回的是名片 JSON 数据
        if (userMsg.includes('生成名片') || userMsg.includes('导出名片')) {
           try {
             // 尝试解析模型返回的真实数据（移除可能存在的 markdown json 标记）
             let jsonStr = aiContent.replace(/```json\n?/g, '').replace(/```\n?/g, '').trim();
             const cardData = JSON.parse(jsonStr);
             
             if (cardData && cardData.name) {
               this.messages.push({
                 role: 'assistant',
                 content: `已为您搜索并生成 ${cardData.name} 的详细学者名片。`,
                 scholarCard: {
                   name: cardData.name || '未知学者',
                   title: cardData.title || '未知职称',
                   org: cardData.org || '未知机构',
                   paperCount: cardData.paperCount || 0,
                   score: cardData.score || 0,
                   research: cardData.research || '暂无研究方向',
                   collaborators: cardData.collaborators || '暂无合作者信息'
                 }
               });
               return; // 成功解析为名片则直接返回，不再展示普通文本
             }
           } catch(e) {
             console.log("未能解析为名片JSON，退回普通文本", aiContent);
           }
        }

        // 如果不是名片，或者是无法解析的名片，直接作为普通文本展示
        this.messages.push({
          role: 'assistant',
          content: aiContent
        });
      } catch (error) {
        console.error('AI Chat Error:', error);
        this.messages.push({
          role: 'assistant',
          content: '连接AI服务失败，请稍后再试或检查API配置。'
        });
      } finally {
        this.loading = false;
        this.scrollToBottom();
      }
    },
    async exportCard(index, name) {
      try {
        const html2canvas = (await import('html2canvas')).default;
        const cardElement = document.getElementById('scholar-card-' + index);
        if (!cardElement) return;

        const canvas = await html2canvas(cardElement, {
          scale: 2,
          backgroundColor: null
        });

        const url = canvas.toDataURL('image/png');
        const link = document.createElement('a');
        link.download = `${name}-学者名片.png`;
        link.href = url;
        link.click();
      } catch (e) {
        console.error('导出名片失败', e);
        alert('导出失败，请检查浏览器支持情况。');
      }
    }
  }
}
</script>

<style scoped>
.ai-chat-overlay {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  transition: all 0.3s ease;
}

.ai-chat-window {
  background: rgba(30, 32, 50, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  transition: opacity 0.3s ease;
}

.resize-handle {
  position: absolute;
  top: 0;
  left: 0;
  width: 15px;
  height: 15px;
  cursor: nwse-resize;
  z-index: 1000;
}

.ai-chat-overlay.minimized .ai-chat-window {
  height: auto !important;
}

.ai-chat-overlay.minimized .resize-handle {
  display: none;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.8) 0%, rgba(118, 75, 162, 0.8) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title h3 {
  margin: 0;
  font-size: 16px;
  color: #fff;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  line-height: 1;
  border-radius: 4px;
}

.action-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-wrapper {
  display: flex;
  width: 100%;
}

.message-wrapper.user {
  justify-content: flex-end;
}

.message-content {
  display: flex;
  gap: 8px;
  max-width: 85%;
}

.message-wrapper.user .message-content {
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.message-wrapper.assistant .avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #e2e8f0;
  word-wrap: break-word;
}

.message-wrapper.assistant .bubble {
  background: rgba(255, 255, 255, 0.05);
  border-top-left-radius: 4px;
}

.message-wrapper.user .bubble {
  background: rgba(102, 126, 234, 0.3);
  border-top-right-radius: 4px;
}

.typing span {
  animation: typing 1.4s infinite;
  display: inline-block;
  margin: 0 1px;
}
.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 100% { opacity: 0.3; transform: translateY(0); }
  50% { opacity: 1; transform: translateY(-2px); }
}

.chat-footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
}

.input-container {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

textarea {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 8px 12px;
  color: #fff;
  font-size: 14px;
  resize: none;
  font-family: inherit;
  outline: none;
  max-height: 100px;
  overflow-y: hidden;
}

textarea::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

textarea:focus {
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(255, 255, 255, 0.1);
}

.send-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  height: 36px;
  font-weight: 500;
  transition: opacity 0.2s;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 学者名片样式 */
.scholar-card-wrapper {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.scholar-card {
  width: 300px;
  background: linear-gradient(145deg, #1e2032 0%, #2a2d46 100%);
  border: 1px solid rgba(102, 126, 234, 0.4);
  border-radius: 12px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.scholar-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.card-avatar {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: bold;
  color: white;
  box-shadow: 0 4px 10px rgba(240, 147, 251, 0.4);
}

.card-info {
  flex: 1;
}

.card-info h4 {
  margin: 0 0 4px 0;
  font-size: 20px;
  color: #fff;
  letter-spacing: 0.5px;
}

.card-title {
  margin: 0 0 4px 0;
  font-size: 13px;
  color: #4facfe;
  font-weight: 500;
}

.card-org {
  margin: 0;
  font-size: 12px;
  color: #a0aec0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-org .icon {
  font-size: 10px;
}

.card-body {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.info-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: 11px;
  color: #718096;
  margin-bottom: 2px;
}

.info-value {
  font-size: 13px;
  color: #e2e8f0;
  line-height: 1.4;
}

.card-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 8px;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.stat-item.divider {
  flex: 0;
  width: 1px;
  height: 30px;
  background: rgba(255, 255, 255, 0.1);
}

.stat-label {
  font-size: 12px;
  color: #a0aec0;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #4facfe;
  font-family: 'Courier New', Courier, monospace;
}

.card-footer {
  text-align: center;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.3);
  letter-spacing: 1px;
  margin-top: 8px;
  border-top: 1px dashed rgba(255, 255, 255, 0.1);
  padding-top: 12px;
}

.logo-text {
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.export-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.export-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 自定义滚动条 */
.chat-body::-webkit-scrollbar {
  width: 0px;
  background: transparent;
}
.chat-body {
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.chat-body::-webkit-scrollbar-track {
  background: transparent;
}
.chat-body::-webkit-scrollbar-thumb {
  background: transparent;
}
</style>
