//
//  NavRoutes.swift
//  PROFI-A
//
//  Дубликат: ru.profia.app.ui.navigation.NavRoutes
//  Все маршруты приложения для дублирования навигации.
//

import Foundation

enum NavRoutes {
    static let splash = "splash"
    static let specialty = "specialty"
    static let businessType = "business_type"
    static let auth = "auth"
    static let home = "home"
    static let projects = "projects"
    static let createProject = "create_project"
    static let addRoom = "add_room"
    static let roomScan = "room_scan"
    static let calculator = "calculator"
    static let profile = "profile"
    static let changePassword = "change_password"
    static let twoFa = "two_fa_settings"
    static let formKs2Ks3 = "form_ks2_ks3"
    static let materials = "materials"
    static let works = "works"
    static let workSection = "works_section"
    static let addWorkTypes = "add_work_types"
    static let workCategory = "work_category"
    static let roomTypes = "room_types"
    static let stages = "stages"
    static let settings = "settings"
    static let subscription = "subscription"
    static let support = "support"
    static let about = "about"
    static let foreman = "foreman"

    static let projectDetail = "project"
    static let editProject = "edit_project"
    static let generalEstimate = "general_estimate"
    static let finalEstimate = "final_estimate"
    static let acts = "acts"
    static let ks2 = "ks2"
    static let ks3 = "ks3"

    static func editProfileSection(_ section: String) -> String { "edit_profile/\(section)" }
    static func addRoom(projectId: String, roomId: String?) -> String {
        if let id = roomId { return "add_room/\(projectId)/\(id)" }
        return "add_room/\(projectId)/new"
    }
    static func workCategory(categoryId: String) -> String { "work_category/\(categoryId)" }
    static func workSection(sectionId: String) -> String { "works_section/\(sectionId)" }
    static func projectDetail(projectId: String) -> String { "project/\(projectId)" }
    static func acts(projectId: String) -> String { "acts/\(projectId)" }
}
