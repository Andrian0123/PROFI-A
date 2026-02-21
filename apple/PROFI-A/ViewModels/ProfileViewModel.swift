//
//  ProfileViewModel.swift
//  PROFI-A
//
//  Дубликат логики профиля. Тип кабинета: PROFI (только профиль в актах) или BUSINESS (профиль + компания + реквизиты).
//

import Foundation
import Combine

final class ProfileViewModel: ObservableObject {
    @Published var userProfile: UserProfile?
    @Published var userAccountType: String = "PROFI" // PROFI | BUSINESS

    func executorLines(for profile: UserProfile?) -> [String]? {
        guard let profile else { return nil }
        var lines: [String] = ["Исполнитель"]
        let isProfi = userAccountType == "PROFI"
        let name = isProfi ? profile.fullName : profile.executorDisplayName(accountType: userAccountType)
        if !name.isEmpty { lines.append(name) }
        if !isProfi {
            if let v = profile.inn, !v.isEmpty { lines.append("ИНН: \(v)") }
            if let v = profile.kpp, !v.isEmpty { lines.append("КПП: \(v)") }
            if let v = profile.legalAddress, !v.isEmpty { lines.append("Адрес: \(v)") }
            if let v = profile.bankName, !v.isEmpty { lines.append("Банк: \(v)") }
            if let v = profile.accountNumber, !v.isEmpty { lines.append("Р/с: \(v)") }
            if let v = profile.bic, !v.isEmpty { lines.append("БИК: \(v)") }
        }
        if !profile.phone.isEmpty { lines.append("Тел.: \(profile.phone)") }
        if !profile.email.isEmpty { lines.append("Email: \(profile.email)") }
        return lines.count > 1 ? lines : nil
    }
}
