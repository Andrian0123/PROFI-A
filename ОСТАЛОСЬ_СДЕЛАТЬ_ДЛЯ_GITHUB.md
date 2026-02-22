# Осталось сделать для загрузки на GitHub

Локально уже выполнено:
- ✅ `git init`
- ✅ `git add .`
- ✅ `git commit -m "Первый коммит: ПРОФЙ-А и backend"`
- ✅ `git branch -M main`

---

## Что нужно сделать вам (2 шага)

### 1. Создать пустой репозиторий на GitHub (если ещё не создали)

1. Откройте [github.com](https://github.com) и войдите.
2. Нажмите **«+» → «New repository»**.
3. **Repository name:** например `PROFI-A`.
4. **Не** ставьте галочку «Add a README file».
5. Нажмите **«Create repository»**.
6. Скопируйте URL репозитория — он вида:  
   `https://github.com/ВАШ_ЛОГИН/PROFI-A.git`

### 2. Выполнить в терминале (подставьте свой URL)

Откройте **Git Bash** или **Git CMD**, перейдите в папку проекта и выполните:

```bash
cd E:\PROFI-A
git remote add origin https://github.com/ВАШ_ЛОГИН/PROFI-A.git
git push -u origin main
```

**Пример:** если ваш логин на GitHub — `andrian0123`, то:

```bash
git remote add origin https://github.com/andrian0123/PROFI-A.git
git push -u origin main
```

При `git push` может открыться окно входа в GitHub — введите логин и пароль (или токен, если GitHub просит не пароль, а токен).

После успешного push откройте в браузере страницу репозитория — там должны быть папки `app`, `server`, `docs` и все файлы проекта.
