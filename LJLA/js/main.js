// LOLA — скрипты сайта
// Пока пусто, можно добавить меню, формы, анимации и т.д.

(function () {
    'use strict';
    // Инициализация при загрузке страницы
    document.addEventListener('DOMContentLoaded', function () {
        // Пример: плавная прокрутка по якорям
        document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
            anchor.addEventListener('click', function (e) {
                var id = this.getAttribute('href');
                if (id === '#') return;
                e.preventDefault();
                var el = document.querySelector(id);
                if (el) el.scrollIntoView({ behavior: 'smooth' });
            });
        });
    });
})();
