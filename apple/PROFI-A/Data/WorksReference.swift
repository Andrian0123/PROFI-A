//
//  WorksReference.swift
//  PROFI-A
//
//  Дубликат: ru.profia.app.data.reference.WorksReference
//  Секции: Внутренние работы, Наружные работы. Группы: Внутренняя отделка, Уличные работы, Потолок, Стены, Пол и др.
//

import Foundation

struct WorkItem {
    let name: String
    let unit: String
    let priceMin: Int
    let priceMax: Int
    let note: String?
    var priceAvg: Int { (priceMin + priceMax) / 2 }
}

struct WorkGroup {
    let title: String
    let items: [WorkItem]
}

struct WorkSection {
    let id: String
    let title: String
    let groups: [WorkGroup]
}

enum WorksReference {
    static let sections: [WorkSection] = [
        WorkSection(
            id: "internal",
            title: "Внутренние работы",
            groups: [
                WorkGroup(title: "Внутренняя отделка", items: [
                    WorkItem(name: "Штукатурка (стены, потолок)", unit: "м²", priceMin: 550, priceMax: 1100, note: nil),
                    WorkItem(name: "Шпаклёвка (под покраску/обои)", unit: "м²", priceMin: 350, priceMax: 600, note: nil),
                    WorkItem(name: "Покраска (стены, потолок)", unit: "м²", priceMin: 300, priceMax: 700, note: nil),
                    WorkItem(name: "Укладка плитки (стены, пол)", unit: "м²", priceMin: 1300, priceMax: 2800, note: nil),
                ]),
                WorkGroup(title: "Потолок", items: []),
                WorkGroup(title: "Стены", items: []),
                WorkGroup(title: "Пол", items: []),
            ]
        ),
        WorkSection(
            id: "external",
            title: "Наружные работы",
            groups: [
                WorkGroup(title: "Уличные работы", items: [
                    WorkItem(name: "Штукатурка фасада", unit: "м²", priceMin: 1800, priceMax: 2500, note: nil),
                    WorkItem(name: "Покраска фасада", unit: "м²", priceMin: 1600, priceMax: 2000, note: nil),
                    WorkItem(name: "Монтаж сайдинга", unit: "м²", priceMin: 900, priceMax: 1600, note: "работа"),
                ]),
            ]
        ),
    ]
}
