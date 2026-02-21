//
//  UserProfile.swift
//  PROFI-A
//
//  Дубликат: ru.profia.app.data.model.UserProfile
//  Для актов: PROFI — только ФИО/тел/email; BUSINESS — + компания + реквизиты.
//

import Foundation

struct UserProfile: Equatable {
    var lastName: String
    var firstName: String
    var middleName: String?
    var email: String
    var phone: String
    var companyName: String?
    var inn: String?
    var kpp: String?
    var legalAddress: String?
    var bankName: String?
    var accountNumber: String?
    var correspondentAccount: String?
    var bic: String?

    /// ФИО одной строкой
    var fullName: String {
        [lastName, firstName, middleName ?? ""]
            .filter { !$0.isEmpty }
            .joined(separator: " ")
    }

    /// Для блока «Исполнитель» в акте: при PROFI — только ФИО; при BUSINESS — companyName или ФИО
    func executorDisplayName(accountType: String) -> String {
        if accountType == "BUSINESS", let name = companyName, !name.isEmpty {
            return name
        }
        return fullName
    }
}
