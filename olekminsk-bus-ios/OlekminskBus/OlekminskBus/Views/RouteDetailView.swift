import SwiftUI

struct RouteDetailView: View {
    let route: BusRoute
    @ObservedObject var viewModel: BusViewModel
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    // Ближайший автобус
                    NextBusCard(route: route, viewModel: viewModel)
                    
                    // Расписание туда
                    ScheduleSection(
                        title: route.forward.name,
                        direction: route.forward,
                        viewModel: viewModel
                    )
                    
                    // Расписание обратно
                    ScheduleSection(
                        title: route.backward.name,
                        direction: route.backward,
                        viewModel: viewModel
                    )
                }
                .padding()
            }
            .navigationTitle("Маршрут \(route.number)")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Закрыть") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        viewModel.toggleFavorite(routeId: route.id)
                    }) {
                        Image(systemName: viewModel.isFavorite(routeId: route.id) ? "heart.fill" : "heart")
                            .foregroundColor(viewModel.isFavorite(routeId: route.id) ? .red : .gray)
                    }
                }
            }
        }
    }
}

struct NextBusCard: View {
    let route: BusRoute
    @ObservedObject var viewModel: BusViewModel
    
    var body: some View {
        let forwardNext = viewModel.getNextBus(for: route.forward)
        let backwardNext = viewModel.getNextBus(for: route.backward)
        
        if let next = forwardNext ?? backwardNext {
            let direction = forwardNext != nil ? route.forward.name : route.backward.name
            
            VStack(spacing: 8) {
                Text("Ближайший автобус")
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.9))
                
                Text(next.time)
                    .font(.system(size: 48, weight: .bold))
                    .foregroundColor(.white)
                
                Text("через \(next.minutes) мин • \(direction)")
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.9))
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [Color.green, Color.green.opacity(0.8)]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .cornerRadius(16)
        }
    }
}

struct ScheduleSection: View {
    let title: String
    let direction: RouteDirection
    @ObservedObject var viewModel: BusViewModel
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.headline)
                .padding(.bottom, 8)
                .overlay(
                    Rectangle()
                        .frame(height: 2)
                        .foregroundColor(.gray.opacity(0.3)),
                    alignment: .bottom
                )
            
            ForEach(direction.times, id: \\.self) { time in
                let isNext = isNextBus(time: time)
                let isPassed = viewModel.isTimePassed(time) && !isNext
                
                HStack {
                    Text(time)
                        .font(.system(size: 18, weight: isNext ? .bold : .regular))
                        .foregroundColor(isPassed ? .gray : (isNext ? .green : .primary))
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(isNext ? Color.green.opacity(0.2) : Color.clear)
                        )
                    
                    Spacer()
                    
                    if !isPassed, let minutes = minutesTo(time: time) {
                        Text("через \(minutes) мин")
                            .font(.subheadline)
                            .foregroundColor(.green)
                    }
                }
                .padding(.vertical, 4)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 2)
    }
    
    func isNextBus(time: String) -> Bool {
        if let next = viewModel.getNextBus(for: direction) {
            return next.time == time
        }
        return false
    }
    
    func minutesTo(time: String) -> Int? {
        let calendar = Calendar.current
        let now = viewModel.currentTime
        let currentMinutes = calendar.component(.hour, from: now) * 60 + calendar.component(.minute, from: now)
        
        let components = time.split(separator: ":").compactMap { Int($0) }
        guard components.count == 2 else { return nil }
        let busMinutes = components[0] * 60 + components[1]
        
        if busMinutes > currentMinutes {
            return busMinutes - currentMinutes
        }
        return nil
    }
}

struct RouteDetailView_Previews: PreviewProvider {
    static var previews: some View {
        RouteDetailView(route: BusRoute.sampleRoutes[0], viewModel: BusViewModel())
    }
}
