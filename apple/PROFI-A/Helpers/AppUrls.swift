//
//  AppUrls.swift
//  PROFI-A
//
//  Ссылки без данных аккаунта: для раздачи пользователям.
//

import Foundation

enum AppUrls {
    /// Сайт / страница приложения (без данных аккаунта)
    static let site = URL(string: "https://andrian0123.github.io/PROFI-A/")!
    /// Для iOS: ссылка на приложение в App Store (подставить id после публикации)
    static let appStore = URL(string: "https://apps.apple.com/app/id123456789")!
    /// Временная ссылка на загрузку (Android APK; на iOS можно открыть сайт)
    static var download: URL { site }
}
