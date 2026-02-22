//
//  ContentView.swift
//  PROFI-A
//
//  Корневой экран. После онбординга/авторизации — Home (проекты + Drawer).
//

import SwiftUI

struct ContentView: View {
    @State private var showDrawer = false
    @State private var currentRoute = NavRoutes.home

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Text("PROFI-A")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                Text("Сметы и акты для строительства и ремонта")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Text("для прораба")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
                    .padding(.top, 4)
            }
            .padding()
            .navigationTitle("Старт")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    HStack(spacing: 12) {
                        Button {
                            UIApplication.shared.open(AppUrls.download)
                        } label: {
                            Text("Скачать")
                        }
                        Button {
                            showDrawer = true
                        } label: {
                            Image(systemName: "line.3.horizontal")
                        }
                    }
                }
            }
            .sheet(isPresented: $showDrawer) {
                DrawerMenuView(onNavigate: { route in
                    currentRoute = route
                    if route == NavRoutes.profile { }
                    else if route == NavRoutes.settings { }
                    else if route == NavRoutes.home { }
                    // Остальные маршруты — заглушки; навигация расширится при добавлении экранов
                }, currentRoute: currentRoute)
            }
        }
    }
}

#Preview {
    ContentView()
}
