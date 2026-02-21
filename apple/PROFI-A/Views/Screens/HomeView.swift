//
//  HomeView.swift
//  PROFI-A
//
//  Дубликат: HomeScreen — список проектов, Drawer, нижняя навигация.
//

import SwiftUI

struct HomeView: View {
    var body: some View {
        NavigationStack {
            List {
                Section("Проекты") {
                    Text("Список проектов (дублировать из Android)")
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle("PROFI-A")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: {}) {
                        Image(systemName: "line.3.horizontal")
                    }
                }
            }
        }
    }
}

#Preview {
    HomeView()
}
