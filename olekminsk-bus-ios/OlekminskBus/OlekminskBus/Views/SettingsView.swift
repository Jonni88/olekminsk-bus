import SwiftUI

struct SettingsView: View {
    @State private var notificationsEnabled = true
    @State private var soundEnabled = true
    @State private var vibrationEnabled = true
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Уведомления")) {
                    Toggle("Включить уведомления", isOn: $notificationsEnabled)
                    Toggle("Звук", isOn: $soundEnabled)
                    Toggle("Вибрация", isOn: $vibrationEnabled)
                }
                
                Section(header: Text("О приложении")) {
                    HStack {
                        Text("Версия")
                        Spacer()
                        Text("1.0.0")
                            .foregroundColor(.gray)
                    }
                    
                    HStack {
                        Text("Город")
                        Spacer()
                        Text("Олёкминск")
                            .foregroundColor(.gray)
                    }
                }
                
                Section {
                    Link(destination: URL(string: "https://github.com/Jonni88/olekminsk-bus")!) {
                        HStack {
                            Text("Исходный код")
                            Spacer()
                            Image(systemName: "arrow.up.right.square")
                                .foregroundColor(.gray)
                        }
                    }
                }
            }
            .navigationTitle("Настройки")
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
    }
}
