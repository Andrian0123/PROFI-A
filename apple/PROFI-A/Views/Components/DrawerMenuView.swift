//
//  DrawerMenuView.swift
//  PROFI-A
//
//  Боковое меню (drawer). Дубликат пунктов из Android NavigationDrawer.
//

import SwiftUI

struct DrawerMenuView: View {
    @Environment(\.dismiss) private var dismiss
    var onNavigate: (String) -> Void
    var currentRoute: String = ""

    var body: some View {
        NavigationStack {
            List {
                Section {
                    drawerRow(title: "Личный кабинет", icon: "person", route: NavRoutes.profile)
                    drawerRow(title: "Мои проекты", icon: "folder", route: NavRoutes.home)
                    drawerRow(title: "Калькулятор", icon: "plus.forwards.minus.backwards", route: NavRoutes.calculator)
                    drawerRow(title: "Скачать приложение (APK)", icon: "square.and.arrow.down", route: "download_app")
                    drawerRow(title: "Добавить прораба / рабочего", icon: "wrench.and.screwdriver", route: NavRoutes.foreman)
                }
                Section("Справочники") {
                    drawerRow(title: "Материалы", icon: "cart", route: NavRoutes.materials)
                    drawerRow(title: "Виды работ", icon: "wrench.and.screwdriver", route: NavRoutes.works)
                    drawerRow(title: "Виды комнат", icon: "house", route: NavRoutes.roomTypes)
                    drawerRow(title: "Этапы", icon: "clock", route: NavRoutes.stages)
                }
                Section("Настройки и поддержка") {
                    drawerRow(title: "Подписка", icon: "creditcard", route: NavRoutes.subscription)
                    drawerRow(title: "Настройки", icon: "gearshape", route: NavRoutes.settings)
                    drawerRow(title: "Тех. поддержка", icon: "questionmark.circle", route: NavRoutes.support)
                    drawerRow(title: "О приложении", icon: "info.circle", route: NavRoutes.about)
                    drawerRow(title: "Поделиться приложением", icon: "square.and.arrow.up", route: "share_app")
                }
                Section("Документы") {
                    drawerRow(title: "Соглашение", icon: "doc.text", route: "agreement")
                    drawerRow(title: "Персональные данные", icon: "doc.text", route: "personal_data")
                    drawerRow(title: "Политика конфиденциальности", icon: "doc.text", route: "privacy_policy")
                }
                Section {
                    Button(role: .destructive) {
                        onNavigate("logout")
                        dismiss()
                    } label: {
                        Label("Выход", systemImage: "xmark")
                    }
                }
            }
            .navigationTitle("PROFI-A")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Закрыть") { dismiss() }
                }
            }
        }
    }

    private func drawerRow(title: String, icon: String, route: String) -> some View {
        Button {
            if route == "download_app" {
                UIApplication.shared.open(AppUrls.download)
                dismiss()
            } else {
                onNavigate(route)
                dismiss()
            }
        } label: {
            Label(title, systemImage: icon)
                .foregroundStyle(currentRoute == route ? Color.accentColor : .primary)
        }
    }
}

#Preview {
    DrawerMenuView(onNavigate: { _ in })
}
