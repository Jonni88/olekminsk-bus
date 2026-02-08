import Foundation
import SwiftUI

enum RouteType: String, Codable {
    case urban = "urban"
    case suburban = "suburban"
    case intercity = "intercity"
    
    var displayName: String {
        switch self {
        case .urban: return "Городской"
        case .suburban: return "Пригород"
        case .intercity: return "Междугородний"
        }
    }
    
    var color: String {
        switch self {
        case .urban: return "blue"
        case .suburban: return "green"
        case .intercity: return "orange"
        }
    }
}

struct BusRoute: Identifiable, Codable {
    let id: Int
    let number: String
    let name: String
    let type: RouteType
    let forward: RouteDirection
    let backward: RouteDirection
    var isFavorite: Bool = false
}

struct RouteDirection: Codable {
    let name: String
    let stops: String
    let times: [String]
}

extension BusRoute {
    static let sampleRoutes: [BusRoute] = [
        BusRoute(id: 1, number: "1", name: "Автовокзал ↔ Центр ↔ Площадь", type: .urban,
                 forward: RouteDirection(name: "Автовокзал → Центр → Площадь", stops: "Автовокзал → Центр → Площадь → Дача",
                                        times: ["06:30", "07:15", "08:00", "09:30", "11:00", "12:30", "14:00", "15:30", "17:00", "18:30", "20:00"]),
                 backward: RouteDirection(name: "Дача → Площадь → Центр", stops: "Дача → Площадь → Центр → Автовокзал",
                                         times: ["07:00", "07:45", "08:30", "10:00", "11:30", "13:00", "14:30", "16:00", "17:30", "19:00", "20:30"])),
        
        BusRoute(id: 2, number: "2", name: "Северный ↔ Рынок ↔ Южный", type: .urban,
                 forward: RouteDirection(name: "Северный → Рынок → Южный", stops: "Северный → Рынок → Автовокзал → Южный",
                                        times: ["06:45", "08:00", "09:30", "11:00", "13:00", "15:00", "17:00", "19:00"]),
                 backward: RouteDirection(name: "Южный → Автовокзал → Рынок", stops: "Южный → Автовокзал → Рынок → Северный",
                                         times: ["07:15", "08:30", "10:00", "11:30", "13:30", "15:30", "17:30", "19:30"])),
        
        BusRoute(id: 5, number: "5", name: "Микрорайон 1 ↔ Микрорайон 2", type: .urban,
                 forward: RouteDirection(name: "Микрорайон 1 → Центр → Микрорайон 2", stops: "Микрорайон 1 → Центр → Микрорайон 2",
                                        times: ["07:00", "09:00", "11:00", "13:00", "15:00", "17:00", "19:00"]),
                 backward: RouteDirection(name: "Микрорайон 2 → Центр → Микрорайон 1", stops: "Микрорайон 2 → Центр → Микрорайон 1",
                                         times: ["07:30", "09:30", "11:30", "13:30", "15:30", "17:30", "19:30"])),
        
        BusRoute(id: 101, number: "101", name: "Олёкминск → Якутск", type: .suburban,
                 forward: RouteDirection(name: "Олёкминск → Якутск", stops: "Олёкминск → Хандыга → Якутск",
                                        times: ["06:00", "08:00", "14:00", "18:00"]),
                 backward: RouteDirection(name: "Якутск → Олёкминск", stops: "Якутск → Хандыга → Олёкминск",
                                         times: ["08:00", "10:00", "16:00", "20:00"])),
        
        BusRoute(id: 102, number: "102", name: "Олёкминск → Тында", type: .intercity,
                 forward: RouteDirection(name: "Олёкминск → Тында", stops: "Олёкминск → Февральск → Тында",
                                        times: ["07:00", "15:00"]),
                 backward: RouteDirection(name: "Тында → Олёкминск", stops: "Тында → Февральск → Олёкминск",
                                         times: ["09:00", "17:00"]))
    ]
}
