# 部署到阿里云云效仓库

## GitHub Secrets 配置

在 GitHub 仓库中配置以下 Secrets（Settings → Secrets and variables → Actions）：

| Secret 名称 | 说明 | 示例值 |
|------------|------|--------|
| `YUNXIAO_USERNAME` | 云效账号/AccessKey ID | `your-username` |
| `YUNXIAO_PASSWORD` | 云效密码/AccessKey Secret | `your-password` |
| `YUNXIAO_SNAPSHOT_URL` | Snapshot 仓库地址 | `https://packages.aliyun.com/maven/repository/snapshot` |
| `YUNXIAO_RELEASE_URL` | Release 仓库地址 | `https://packages.aliyun.com/maven/repository/release` |

## 获取云效凭证

### 方式一：使用个人账号

1. 登录 [云效](https://www.aliyun.com/product/yunxiao)
2. 进入项目 → 设置 → Maven 仓库
3. 查看 Snapshot 和 Release 仓库地址
4. 使用账号密码作为凭证

### 方式二：使用 AccessKey（推荐）

1. 访问 [阿里云 AccessKey 管理](https://ram.console.aliyun.com/manage/ak)
2. 创建 AccessKey
3. 使用 AccessKey ID 作为 `YUNXIAO_USERNAME`
4. 使用 AccessKey Secret 作为 `YUNXIAO_PASSWORD`

## 触发发布

### 自动发布 - Snapshot 版本

每次推送到 `main` 分支时，自动发布 Snapshot 版本到云效仓库：

```bash
git push origin main
```

### 自动发布 - Release 版本

创建 GitHub Release 或推送 tag 时，自动发布 Release 版本：

```bash
# 创建并推送 tag
git tag v1.0.0
git push origin v1.0.0
```

### 手动触发

在 GitHub Actions 页面手动运行 workflow。

## 本地发布

配置 `~/.gradle/gradle.properties`：

```properties
YunxiaoSnapshotUsername=your-username
YunxiaoSnapshotPassword=your-password
YunxiaoSnapshotRepositoryUrl=https://packages.aliyun.com/maven/repository/snapshot
YunxiaoReleaseUsername=your-username
YunxiaoReleasePassword=your-password
YunxiaoReleaseRepositoryUrl=https://packages.aliyun.com/maven/repository/release
```

然后执行：

```bash
# 发布 Snapshot
./gradlew publishAllPublicationsToYunxiaoSnapshotRepository

# 发布 Release
./gradlew publishAllPublicationsToYunxiaoReleaseRepository
```

## 仓库地址配置

根据你的云效项目，仓库地址可能是：

- **公共云**: `https://packages.aliyun.com/maven/repository/...`
- **私有云/企业版**: `https://repos.xx.xxxx.com/...`

请根据实际部署环境修改 GitHub Secrets 和 `build.gradle.kts` 中的 URL。
