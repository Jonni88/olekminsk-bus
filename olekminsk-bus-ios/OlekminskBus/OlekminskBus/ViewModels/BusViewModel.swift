import Foundation
import SwiftUI
import Combine

class BusViewModel: ObservableObject {
    @Published var routes: [BusRoute] = []
    @Published var favorites: [Int] = []
    @Published var currentTime = Date()
    
    private var cancellables = Set<AnyCancellable>()
    private let favoritesKey = "favorites"
    
    init() {
        loadRoutes()
        loadFavorites()
        startTimer()
    }
    
    private func startTimer() {
        Timer.publish(every: 1, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                self?.currentTime = Date()
            }
            .store(in: &cancellables)
    }
    
    private func loadRoutes() {
        // В реальном приложении здесь загрузка с сервера
        routes = BusRoute.sampleRoutes
    }
    
    private func loadFavorites() {
        if let saved = UserDefaults.standard.array(forKey: favoritesKey) as? [Int] {
            favorites = saved
        }
    }
    
    private func saveFavorites() {
        UserDefaults.standard.set(favorites, forKey: favoritesKey)
    }
    
    func toggleFavorite(routeId: Int) {
        if favorites.contains(routeId) {
            favorites.removeAll { $0 == routeId }
        } else {
            favorites.append(routeId)
        }
        saveFavorites()
    }
    
    func isFavorite(routeId: Int) -> Bool {
        return favorites.contains(routeId)
    }
    
    var favoriteRoutes: [BusRoute] {
        routes.filter { favorites.contains($0.id) }
    }
    
    var suburbanRoutes: [BusRoute] {
        routes.filter { $0.type == .suburban }
    }
    
    func getNextBus(for direction: RouteDirection) -> (time: String, minutes: Int)? {
        let calendar = Calendar.current
        let now = currentTime
        let currentMinutes = calendar.component(.hour, from: now) * 60 + calendar.component(.minute, from: now)
        
        for time in direction.times {
            let components = time.split(separator: ":").compactMap { Int($0) }
            guard components.count == 2 else { continue }
            let busMinutes = components[0] * 60 + components[1]
            if busMinutes > currentMinutes {
                return (time, busMinutes - currentMinutes)
            }
        }
        return nil
    }
    
    func isTimePassed(_ time: String) -> Bool {
        let calendar = Calendar.current
        let now = currentTime
        let currentMinutes = calendar.component(.hour, from: now) * 60 + calendar.component(.minute, from: now)
        
        let components = time.split(separator: ":").compactMap { Int($0) }
        guard components.count == 2 else { return false }
        let busMinutes = components[0] * 60 + components[1]
        
        return busMinutes < currentMinutes
    }
}
