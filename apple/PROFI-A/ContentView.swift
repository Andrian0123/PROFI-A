//
//  ContentView.swift
//  PROFI-A
//
//  Корневой экран. После онбординга/авторизации — Home (проекты + Drawer).
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Text("PROFI-A")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                Text("Сметный калькулятор (iOS)")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Text("Дубликат решений из Android-версии")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
                    .padding(.top, 8)
            }
            .padding()
            .navigationTitle("Старт")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Menu {
                        Button("Профиль") { }
                        Button("Настройки") { }
                    } label: {
                        Image(systemName: "line.3.horizontal")
                    }
                }
            }
        }
    }
}

#Preview {
    ContentView()
}
