# 死了么 APP 后端API接口详细设计说明

> 版本: V1.1 MVP  
> 日期: 2026-01-10  
> 状态: 修订版（收敛 MVP 范围）  
> 基础路径: `https://api.silemore.com/api/v1`

---

## 0. MVP 接口范围

> [!IMPORTANT]
> 下表明确标注 MVP 版本必须实现的接口与暂缓接口。暂缓接口在后续版本实现。

| 模块 | MVP 保留接口 | MVP 暂缓接口 |
|------|--------------|---------------|
| **认证** | register, login, logout | refresh-token, forgot-password, reset-password |
| **用户** | /me, /me/settings, /me/pause | /me/change-password, DELETE /me |
| **签到** | POST /check-ins, /check-ins/today, GET /check-ins | /check-ins/stats, /check-ins/calendar |
| **联系人** | POST/GET/DELETE /contacts, /contacts/verify | /contacts/{id} (GET/PATCH), /contacts/{id}/resend-verification |
| **通知** | (无) | GET /notifications |
| **限流** | (简化) | Redis 黑名单、复杂限流规则 |

**MVP 简化说明:**
- 无 Refresh Token，Access Token 有效期延长至 **7天**
- 登出仅清除客户端 Token，无服务端黑名单
- 签到接口无 mood/note 字段
- 联系人仅支持添加/删除/验证

---

## 1. 接口规范

### 1.1 通用规范

| 项目 | 规范 |
|-----|------|
| 协议 | HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 时间格式 | ISO 8601 (`2026-01-10T12:00:00+08:00`) |
| 分页参数 | `page` (从0开始), `size` (默认20, 最大100) |

### 1.2 请求头规范

| Header | 说明 | 必填 |
|--------|------|-----|
| `Content-Type` | `application/json` | 是 |
| `Authorization` | `Bearer {token}` | 需认证接口必填 |
| `Accept-Language` | 语言偏好，如 `zh-CN` | 否 |
| `X-Request-Id` | 请求追踪ID (UUID) | 否 |

### 1.3 通用响应格式

#### 成功响应
```json
{
    "code": 200,
    "message": "success",
    "data": { },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

#### 错误响应
```json
{
    "code": 400,
    "message": "参数错误",
    "errors": [
        {
            "field": "email",
            "message": "邮箱格式不正确"
        }
    ],
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

### 1.4 HTTP状态码

| 状态码 | 说明 | 使用场景 |
|-------|------|---------|
| 200 | OK | 请求成功 |
| 201 | Created | 资源创建成功 |
| 204 | No Content | 删除成功 |
| 400 | Bad Request | 请求参数错误 |
| 401 | Unauthorized | 未认证或Token失效 |
| 403 | Forbidden | 无权限访问 |
| 404 | Not Found | 资源不存在 |
| 409 | Conflict | 资源冲突 |
| 422 | Unprocessable Entity | 业务逻辑错误 |
| 429 | Too Many Requests | 请求频率超限 |
| 500 | Internal Server Error | 服务器内部错误 |

### 1.5 业务错误码

| 错误码 | 说明 |
|-------|------|
| 10001 | 邮箱已被注册 |
| 10002 | 邮箱或密码错误 |
| 10003 | 账号已被禁用 |
| 10004 | 验证码错误或已过期 |
| 20001 | 今日已签到 |
| 20002 | 签到服务暂时不可用 |
| 30001 | 紧急联系人数量已达上限 |
| 30002 | 该邮箱已被添加为联系人 |
| 30003 | 验证链接已过期 |

---

## 2. 认证接口

### 2.1 用户注册

注册新用户账号。

```
POST /users/register
```

**请求体:**
```json
{
    "email": "zhangsan@example.com",
    "password": "Password123!",
    "nickname": "张三",
    "agreeTerms": true
}
```

**请求参数说明:**

| 参数 | 类型 | 必填 | 验证规则 | 说明 |
|-----|------|-----|---------|------|
| email | string | 是 | 有效邮箱格式，最大100字符 | 用户邮箱，作为登录账号 |
| password | string | 是 | 8-32字符，包含字母和数字 | 登录密码 |
| nickname | string | 是 | 2-50字符 | 用户昵称 |
| agreeTerms | boolean | 是 | 必须为true | 同意用户协议 |

**成功响应 (201 Created):**
```json
{
    "code": 201,
    "message": "注册成功",
    "data": {
        "id": 1,
        "email": "zhangsan@example.com",
        "nickname": "张三",
        "createdAt": "2026-01-10T12:00:00+08:00"
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**错误响应 (409 Conflict):**
```json
{
    "code": 10001,
    "message": "该邮箱已被注册",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**业务逻辑:**
1. 验证邮箱格式和密码强度
2. 检查邮箱是否已被注册
3. 对密码进行BCrypt加密(cost=12)
4. 创建用户记录，设置默认配置(alertDays=3, reminderTime=20:00)
5. 发送欢迎邮件(异步)
6. 返回用户基本信息

---

### 2.2 用户登录

用户登录获取访问令牌。

```
POST /users/login
```

**请求体:**
```json
{
    "email": "zhangsan@example.com",
    "password": "Password123!",
    "rememberMe": true
}
```

**请求参数说明:**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| email | string | 是 | 注册邮箱 |
| password | string | 是 | 登录密码 |
| rememberMe | boolean | 否 | 是否延长Token有效期(默认false) |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
        "tokenType": "Bearer",
        "expiresIn": 86400,
        "user": {
            "id": 1,
            "email": "zhangsan@example.com",
            "nickname": "张三",
            "alertDays": 3,
            "reminderTime": "20:00",
            "isPaused": false,
            "hasCheckedInToday": true,
            "streakDays": 15
        }
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**响应字段说明:**

| 字段 | 类型 | 说明 |
|-----|------|------|
| accessToken | string | JWT访问令牌 |
| refreshToken | string | 刷新令牌 |
| tokenType | string | 令牌类型，固定为"Bearer" |
| expiresIn | integer | Access Token有效期(秒) |
| user | object | 用户信息对象 |
| user.hasCheckedInToday | boolean | 今日是否已签到 |
| user.streakDays | integer | 连续签到天数 |

**错误响应 (401 Unauthorized):**
```json
{
    "code": 10002,
    "message": "邮箱或密码错误",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**Token有效期:**

| rememberMe | Access Token | Refresh Token |
|------------|--------------|---------------|
| false | 24小时 | 7天 |
| true | 7天 | 30天 |

---

### 2.3 刷新令牌

> [!NOTE]
> **MVP 暂缓**: 简化认证流程，使用长期 Access Token (7天)，无需 Refresh Token。

使用Refresh Token获取新的Access Token。

```
POST /users/refresh-token
```

**请求体:**
```json
{
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "refreshToken": "bmV3IHJlZnJlc2ggdG9rZW4...",
        "tokenType": "Bearer",
        "expiresIn": 86400
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**说明:** 每次刷新会同时返回新的Refresh Token，旧Token失效。

---

### 2.4 发送密码重置邮件

> [!NOTE]
> **MVP 暂缓**: 优先实现核心签到流程，密码找回在后续版本实现。

请求发送密码重置邮件。

```
POST /users/forgot-password
```

**请求体:**
```json
{
    "email": "zhangsan@example.com"
}
```

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "如果该邮箱已注册，您将收到密码重置邮件",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**安全说明:** 无论邮箱是否存在，均返回相同响应，防止邮箱枚举攻击。

**邮件内容:**
- 包含重置链接: `https://app.silemore.com/reset-password?token={resetToken}`
- 链接有效期: 30分钟
- 每个邮箱每小时最多请求3次

---

### 2.5 重置密码

通过重置链接设置新密码。

```
POST /users/reset-password
```

**请求体:**
```json
{
    "token": "reset-token-from-email",
    "newPassword": "NewPassword456!"
}
```

**请求参数说明:**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| token | string | 是 | 邮件中的重置Token |
| newPassword | string | 是 | 新密码，需满足密码强度要求 |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "密码重置成功，请使用新密码登录",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**错误响应 (400 Bad Request):**
```json
{
    "code": 10004,
    "message": "重置链接已过期，请重新申请",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

### 2.6 用户登出

登出并使Token失效。

```
POST /users/logout
Authorization: Bearer {token}
```

**成功响应 (204 No Content):** 无响应体

**业务逻辑 (MVP 简化):**
1. ~~将当前Token加入黑名单(Redis存储)~~ → 仅清除客户端 Token
2. 服务端不维护黑名单，Token 过期后自动失效

---

## 3. 用户管理接口

### 3.1 获取当前用户信息

获取当前登录用户的详细信息。

```
GET /users/me
Authorization: Bearer {token}
```

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "email": "zhangsan@example.com",
        "nickname": "张三",
        "alertDays": 3,
        "reminderTime": "20:00",
        "reminderEnabled": true,
        "isPaused": false,
        "pauseUntil": null,
        "createdAt": "2026-01-01T10:00:00+08:00",
        "updatedAt": "2026-01-10T08:00:00+08:00",
        "stats": {
            "totalCheckIns": 150,
            "currentStreak": 15,
            "longestStreak": 30,
            "lastCheckInAt": "2026-01-10T08:30:00+08:00"
        },
        "contacts": {
            "total": 2,
            "verified": 2
        }
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**响应字段说明:**

| 字段 | 类型 | 说明 |
|-----|------|------|
| alertDays | integer | 预警天数阈值(1-7) |
| reminderTime | string | 每日提醒时间(HH:mm) |
| reminderEnabled | boolean | 是否启用签到提醒 |
| isPaused | boolean | 是否暂停监测 |
| pauseUntil | string/null | 暂停截止时间 |
| stats | object | 签到统计信息 |
| stats.totalCheckIns | integer | 总签到次数 |
| stats.currentStreak | integer | 当前连续签到天数 |
| stats.longestStreak | integer | 历史最长连续签到 |
| contacts | object | 联系人统计 |

---

### 3.2 更新用户资料

更新当前用户的基本资料。

```
PATCH /users/me
Authorization: Bearer {token}
```

**请求体:**
```json
{
    "nickname": "张三丰"
}
```

**可更新字段:**

| 字段 | 类型 | 验证规则 |
|-----|------|---------|
| nickname | string | 2-50字符 |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "更新成功",
    "data": {
        "id": 1,
        "email": "zhangsan@example.com",
        "nickname": "张三丰",
        "updatedAt": "2026-01-10T12:00:00+08:00"
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

### 3.3 更新用户设置

更新用户的预警和提醒设置。

```
PATCH /users/me/settings
Authorization: Bearer {token}
```

**请求体:**
```json
{
    "alertDays": 5,
    "reminderTime": "21:00",
    "reminderEnabled": true
}
```

**可更新字段:**

| 字段 | 类型 | 验证规则 | 说明 |
|-----|------|---------|------|
| alertDays | integer | 1-7 | 预警天数阈值 |
| reminderTime | string | HH:mm格式 | 每日提醒时间 |
| reminderEnabled | boolean | - | 是否启用提醒 |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "设置已更新",
    "data": {
        "alertDays": 5,
        "reminderTime": "21:00",
        "reminderEnabled": true,
        "isPaused": false,
        "pauseUntil": null
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

### 3.4 暂停/恢复监测

暂停或恢复预警监测功能。

```
POST /users/me/pause
Authorization: Bearer {token}
```

**请求体 - 暂停监测:**
```json
{
    "action": "pause",
    "duration": 7,
    "reason": "出国旅行"
}
```

**请求体 - 恢复监测:**
```json
{
    "action": "resume"
}
```

**请求参数说明:**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| action | string | 是 | "pause" 或 "resume" |
| duration | integer | 暂停时是 | 暂停天数(1-30) |
| reason | string | 否 | 暂停原因(最大200字符) |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "监测已暂停",
    "data": {
        "isPaused": true,
        "pauseUntil": "2026-01-17T23:59:59+08:00",
        "reason": "出国旅行"
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**业务逻辑:**
- 暂停期间不发送预警通知
- 到期自动恢复，或用户手动恢复
- 暂停期间签到不影响连续签到天数计算

---

### 3.5 修改密码

修改当前用户密码。

```
POST /users/me/change-password
Authorization: Bearer {token}
```

**请求体:**
```json
{
    "currentPassword": "OldPassword123!",
    "newPassword": "NewPassword456!"
}
```

**请求参数说明:**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| currentPassword | string | 是 | 当前密码 |
| newPassword | string | 是 | 新密码，需满足密码强度要求 |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "密码修改成功",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**错误响应 (400 Bad Request):**
```json
{
    "code": 400,
    "message": "当前密码不正确",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**安全措施:**
- 修改密码后使所有现有Token失效
- 发送密码变更通知邮件

---

### 3.6 注销账号

永久删除用户账号及所有相关数据。

```
DELETE /users/me
Authorization: Bearer {token}
```

**请求体:**
```json
{
    "password": "Password123!",
    "confirmation": "DELETE"
}
```

**请求参数说明:**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| password | string | 是 | 当前密码确认 |
| confirmation | string | 是 | 必须为"DELETE" |

**成功响应 (204 No Content):** 无响应体

**业务逻辑:**
1. 验证密码
2. 向所有已验证的紧急联系人发送账号注销通知
3. 删除所有用户数据(级联删除签到记录、联系人等)
4. 发送账号注销确认邮件

---

## 4. 签到接口

### 4.1 执行签到

完成每日签到。

```
POST /check-ins
Authorization: Bearer {token}
```

**请求体 (MVP 简化, 无 mood/note):**
```json
{}
```

> [!NOTE]
> **心情备注功能 MVP 暂缓**，后续版本支持以下字段：

```json
// 后续版本支持
{
    "mood": "happy",
    "note": "今天状态不错"
}
```

**可选参数:**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| mood | string | 否 | 心情标签: happy, normal, sad, tired |
| note | string | 否 | 备注(最大200字符) |

**成功响应 (201 Created):**
```json
{
    "code": 201,
    "message": "签到成功",
    "data": {
        "id": 1234,
        "checkInDate": "2026-01-10",
        "checkInTime": "2026-01-10T12:30:00+08:00",
        "mood": "happy",
        "note": "今天状态不错",
        "streakDays": 16,
        "isNewRecord": false,
        "encouragement": "连续签到16天，继续保持！"
    },
    "timestamp": "2026-01-10T12:30:00+08:00"
}
```

**响应字段说明:**

| 字段 | 类型 | 说明 |
|-----|------|------|
| streakDays | integer | 当前连续签到天数 |
| isNewRecord | boolean | 是否创造新的连续签到记录 |
| encouragement | string | 鼓励语 |

**错误响应 (409 Conflict):**
```json
{
    "code": 20001,
    "message": "今日已签到",
    "data": {
        "checkInTime": "2026-01-10T08:30:00+08:00",
        "streakDays": 16
    },
    "timestamp": "2026-01-10T12:30:00+08:00"
}
```

**业务逻辑 (MVP 简化):**
1. 检查今日是否已签到 ~~(先查Redis缓存)~~ → 直接查数据库
2. 创建签到记录
3. 计算连续签到天数
4. ~~清除Redis中的未签到状态~~
5. 如果之前处于预警状态，发送恢复通知给紧急联系人

---

### 4.2 获取今日签到状态

查询当前用户今日的签到状态。

```
GET /check-ins/today
Authorization: Bearer {token}
```

**成功响应 (200 OK) - 已签到:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "hasCheckedIn": true,
        "checkIn": {
            "id": 1234,
            "checkInDate": "2026-01-10",
            "checkInTime": "2026-01-10T08:30:00+08:00",
            "mood": "happy"
        },
        "stats": {
            "currentStreak": 16,
            "missedDays": 0,
            "alertThreshold": 3
        }
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**成功响应 (200 OK) - 未签到:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "hasCheckedIn": false,
        "checkIn": null,
        "stats": {
            "currentStreak": 15,
            "missedDays": 0,
            "alertThreshold": 3,
            "lastCheckInAt": "2026-01-09T20:15:00+08:00"
        }
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

### 4.3 获取签到历史

分页查询签到历史记录。

```
GET /check-ins?page=0&size=20&startDate=2026-01-01&endDate=2026-01-10
Authorization: Bearer {token}
```

**请求参数:**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | integer | 否 | 0 | 页码(从0开始) |
| size | integer | 否 | 20 | 每页条数(最大100) |
| startDate | string | 否 | - | 开始日期(YYYY-MM-DD) |
| endDate | string | 否 | - | 结束日期(YYYY-MM-DD) |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "content": [
            {
                "id": 1234,
                "checkInDate": "2026-01-10",
                "checkInTime": "2026-01-10T08:30:00+08:00",
                "mood": "happy",
                "note": null
            },
            {
                "id": 1233,
                "checkInDate": "2026-01-09",
                "checkInTime": "2026-01-09T20:15:00+08:00",
                "mood": "normal",
                "note": null
            }
        ],
        "page": 0,
        "size": 20,
        "totalElements": 150,
        "totalPages": 8
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

### 4.4 获取签到统计

> [!NOTE]
> **MVP 暂缓**: 统计功能在后续版本实现。

获取签到统计数据。

```
GET /check-ins/stats?period=month&date=2026-01
Authorization: Bearer {token}
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| period | string | 否 | 统计周期: week, month, year |
| date | string | 否 | 指定日期(YYYY-MM 或 YYYY) |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "period": "month",
        "date": "2026-01",
        "summary": {
            "totalDays": 10,
            "checkedInDays": 9,
            "missedDays": 1,
            "checkInRate": 90.0,
            "currentStreak": 5,
            "longestStreak": 5
        },
        "calendar": [
            {"date": "2026-01-01", "checkedIn": true},
            {"date": "2026-01-02", "checkedIn": true},
            {"date": "2026-01-03", "checkedIn": false},
            {"date": "2026-01-04", "checkedIn": true}
        ],
        "moodDistribution": {
            "happy": 5,
            "normal": 3,
            "sad": 0,
            "tired": 1
        }
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

### 4.5 获取签到日历

> [!NOTE]
> **MVP 暂缓**: 日历视图在后续版本实现。

获取指定月份的签到日历视图。

```
GET /check-ins/calendar?year=2026&month=1
Authorization: Bearer {token}
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| year | integer | 是 | 年份 |
| month | integer | 是 | 月份(1-12) |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "year": 2026,
        "month": 1,
        "days": [
            {
                "date": "2026-01-01",
                "dayOfWeek": 4,
                "checkedIn": true,
                "checkInTime": "2026-01-01T09:00:00+08:00",
                "mood": "happy"
            },
            {
                "date": "2026-01-02",
                "dayOfWeek": 5,
                "checkedIn": true,
                "checkInTime": "2026-01-02T08:30:00+08:00",
                "mood": "normal"
            }
        ],
        "stats": {
            "checkedInDays": 9,
            "totalDays": 10,
            "rate": 90.0
        }
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

## 5. 紧急联系人接口

### 5.1 获取联系人列表

获取当前用户的紧急联系人列表。

```
GET /contacts
Authorization: Bearer {token}
```

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "contacts": [
            {
                "id": 1,
                "name": "李四",
                "email": "li**@example.com",
                "isVerified": true,
                "createdAt": "2026-01-01T10:00:00+08:00"
            },
            {
                "id": 2,
                "name": "王五",
                "email": "wa**@example.com",
                "isVerified": false,
                "createdAt": "2026-01-05T14:00:00+08:00"
            }
        ],
        "total": 2,
        "limit": 5,
        "remaining": 3
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**响应字段说明:**

| 字段 | 类型 | 说明 |
|-----|------|------|
| email | string | 脱敏后的邮箱 |
| isVerified | boolean | 联系人是否已验证 |
| limit | integer | 联系人数量上限 |
| remaining | integer | 还可添加的数量 |

---

### 5.2 添加联系人

添加新的紧急联系人。

```
POST /contacts
Authorization: Bearer {token}
```

**请求体:**
```json
{
    "name": "赵六",
    "email": "zhaoliu@example.com",
    "relationship": "朋友",
    "message": "你好，我已将您设为紧急联系人..."
}
```

**请求参数说明:**

| 参数 | 类型 | 必填 | 验证规则 | 说明 |
|-----|------|-----|---------|------|
| name | string | 是 | 2-50字符 | 联系人姓名 |
| email | string | 是 | 有效邮箱格式 | 联系人邮箱 |
| relationship | string | 否 | 最大20字符 | 关系(朋友/家人/同事等) |
| message | string | 否 | 最大500字符 | 发送给联系人的自定义消息 |

**成功响应 (201 Created):**
```json
{
    "code": 201,
    "message": "添加成功，验证邮件已发送至联系人邮箱",
    "data": {
        "id": 3,
        "name": "赵六",
        "email": "zh**@example.com",
        "relationship": "朋友",
        "isVerified": false,
        "verifyEmailSentAt": "2026-01-10T12:00:00+08:00",
        "createdAt": "2026-01-10T12:00:00+08:00"
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**错误响应 (400 Bad Request) - 达到上限:**
```json
{
    "code": 30001,
    "message": "紧急联系人数量已达上限(5人)",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**错误响应 (409 Conflict) - 重复添加:**
```json
{
    "code": 30002,
    "message": "该邮箱已被添加为联系人",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**验证邮件内容:**
```
主题: 【死了么】{用户昵称} 邀请您成为紧急联系人

{联系人姓名} 您好！

{用户昵称} 使用"死了么"安全监测应用，并希望将您设为紧急联系人。

{如有自定义消息，显示在此}

"死了么"是一款为独居人群设计的安全工具。如果用户连续多日未签到，
系统会自动向您发送邮件通知，请您帮忙确认其安全状况。

如果您同意成为紧急联系人，请点击下方链接确认：
{确认链接}

链接有效期: 7天

如果您不认识发件人，请忽略此邮件。

—— 死了么 安全保障系统
```

---

### 5.3 获取联系人详情

获取单个联系人的详细信息。

```
GET /contacts/{id}
Authorization: Bearer {token}
```

**路径参数:**

| 参数 | 类型 | 说明 |
|-----|------|------|
| id | long | 联系人ID |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "name": "李四",
        "email": "lisi@example.com",
        "relationship": "朋友",
        "isVerified": true,
        "verifiedAt": "2026-01-02T15:00:00+08:00",
        "createdAt": "2026-01-01T10:00:00+08:00",
        "updatedAt": "2026-01-02T15:00:00+08:00",
        "notificationHistory": [
            {
                "id": 101,
                "type": "ALERT",
                "status": "SENT",
                "sentAt": "2026-01-08T00:30:00+08:00"
            }
        ]
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

### 5.4 更新联系人

更新联系人信息。

```
PATCH /contacts/{id}
Authorization: Bearer {token}
```

**请求体:**
```json
{
    "name": "李四丰",
    "relationship": "家人"
}
```

**可更新字段:**

| 字段 | 类型 | 说明 |
|-----|------|------|
| name | string | 联系人姓名(2-50字符) |
| relationship | string | 关系(最大20字符) |

**注意:** 邮箱不可更新，如需更换邮箱请删除后重新添加。

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "更新成功",
    "data": {
        "id": 1,
        "name": "李四丰",
        "email": "li**@example.com",
        "relationship": "家人",
        "isVerified": true,
        "updatedAt": "2026-01-10T12:00:00+08:00"
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

### 5.5 删除联系人

删除紧急联系人。

```
DELETE /contacts/{id}
Authorization: Bearer {token}
```

**成功响应 (204 No Content):** 无响应体

**业务逻辑:**
- 向被删除的联系人发送通知邮件
- 级联删除相关的通知记录

---

### 5.6 重新发送验证邮件

重新发送联系人验证邮件。

```
POST /contacts/{id}/resend-verification
Authorization: Bearer {token}
```

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "验证邮件已重新发送",
    "data": {
        "sentAt": "2026-01-10T12:00:00+08:00",
        "expiresAt": "2026-01-17T12:00:00+08:00"
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**限制:** 每个联系人每24小时最多发送3次验证邮件。

---

### 5.7 验证联系人 (公开接口)

联系人点击验证链接后的确认接口。

```
POST /contacts/verify
```

**注意:** 此接口无需认证，通过Token验证身份。

**请求体:**
```json
{
    "token": "verification-token-from-email"
}
```

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "验证成功，您已成为紧急联系人",
    "data": {
        "userName": "张三",
        "contactName": "李四"
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

**错误响应 (400 Bad Request):**
```json
{
    "code": 30003,
    "message": "验证链接已过期，请联系用户重新发送",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

## 6. 通知记录接口

> [!NOTE]
> **MVP 暂缓**: 通知历史查询在后续版本实现。

### 6.1 获取通知历史

获取系统发送的通知记录。

```
GET /notifications?page=0&size=20&type=ALERT
Authorization: Bearer {token}
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| page | integer | 否 | 页码(默认0) |
| size | integer | 否 | 每页条数(默认20) |
| type | string | 否 | 通知类型: ALERT, RECOVERY |
| status | string | 否 | 发送状态: SENT, FAILED |

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "content": [
            {
                "id": 101,
                "type": "ALERT",
                "status": "SENT",
                "contact": {
                    "id": 1,
                    "name": "李四",
                    "email": "li**@example.com"
                },
                "missedDays": 3,
                "sentAt": "2026-01-08T00:30:00+08:00"
            },
            {
                "id": 102,
                "type": "RECOVERY",
                "status": "SENT",
                "contact": {
                    "id": 1,
                    "name": "李四",
                    "email": "li**@example.com"
                },
                "sentAt": "2026-01-09T08:30:00+08:00"
            }
        ],
        "page": 0,
        "size": 20,
        "totalElements": 5,
        "totalPages": 1
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

## 7. 系统接口

### 7.1 健康检查

检查服务健康状态。

```
GET /health
```

**成功响应 (200 OK):**
```json
{
    "status": "UP",
    "components": {
        "db": {"status": "UP"},
        "mail": {"status": "UP"}
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

### 7.2 获取应用版本

获取当前API版本信息。

```
GET /version
```

**成功响应 (200 OK):**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "version": "1.0.0",
        "buildTime": "2026-01-01T00:00:00+08:00",
        "gitCommit": "abc1234"
    },
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

## 8. 接口限流规则

> [!NOTE]
> **MVP 简化**: 不使用 Redis 实现等复杂限流，仅依赖应用层基础限流 + 日志监控。

### 8.1 MVP 限流策略

| 维度 | 策略 |
|------|------|
| 全局 | Nginx 层限流 (100请求/分钟/IP) |
| 关键接口 | 应用层日志记录，异常人工处理 |

### 8.2 后续版本完整限流 (暂缓)

以下为后续版本设计，MVP 不实现：

### 8.2.1 全局限流

| 限流维度 | 限制 |
|---------|------|
| IP | 100次/分钟 |
| 用户 | 60次/分钟 |

### 8.2 特定接口限流

| 接口 | 限流规则 |
|-----|---------|
| POST /users/register | 5次/IP/小时 |
| POST /users/login | 10次/IP/分钟 |
| POST /users/forgot-password | 3次/邮箱/小时 |
| POST /check-ins | 10次/用户/分钟 |
| POST /contacts | 10次/用户/小时 |
| POST /contacts/{id}/resend-verification | 3次/联系人/24小时 |

### 8.3 限流响应

```
HTTP/1.1 429 Too Many Requests
Retry-After: 60
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1704844860
```

```json
{
    "code": 429,
    "message": "请求过于频繁，请稍后再试",
    "retryAfter": 60,
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

---

## 9. 安全规范

### 9.1 JWT Token结构

```
Header:
{
    "alg": "HS256",
    "typ": "JWT"
}

Payload:
{
    "sub": "1",                    // 用户ID
    "email": "user@example.com",   // 用户邮箱
    "iat": 1704844800,             // 签发时间
    "exp": 1704931200              // 过期时间
}
```

### 9.2 认证错误响应

```json
{
    "code": 401,
    "message": "Token已过期，请重新登录",
    "timestamp": "2026-01-10T12:00:00+08:00"
}
```

### 9.3 敏感数据处理

所有API响应中的敏感数据都进行脱敏处理：

- **邮箱**: `zhangsan@example.com` → `zh**@example.com`
- **手机号**: `13812345678` → `138****5678`

---

## 附录

### A. 枚举值定义

#### 通知类型 (NotificationType)
| 值 | 说明 |
|---|------|
| ALERT | 预警通知 |
| RECOVERY | 恢复通知 |
| WELCOME | 欢迎通知 |
| REMOVED | 移除通知 |

#### 通知状态 (NotificationStatus)
| 值 | 说明 |
|---|------|
| PENDING | 待发送 |
| SENT | 已发送 |
| FAILED | 发送失败 |

#### 心情标签 (Mood)
| 值 | 说明 |
|---|------|
| happy | 开心 |
| normal | 一般 |
| sad | 难过 |
| tired | 疲惫 |

### B. 变更日志

| 版本 | 日期 | 说明 |
|-----|------|------|
| 1.0.0 | 2026-01-10 | 初始版本 |
