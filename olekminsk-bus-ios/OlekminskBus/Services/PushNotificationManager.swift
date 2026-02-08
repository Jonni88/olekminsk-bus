import FirebaseMessaging
import UserNotifications
import UIKit

class PushNotificationManager: NSObject, MessagingDelegate, UNUserNotificationCenterDelegate {
    
    static let shared = PushNotificationManager()
    
    var fcmToken: String?
    
    func registerForPushNotifications() {
        UNUserNotificationCenter.current().delegate = self
        
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions) { granted, error in
            if granted {
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            }
        }
        
        Messaging.messaging().delegate = self
    }
    
    // MARK: - MessagingDelegate
    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        self.fcmToken = fcmToken
        print("FCM Token: \(fcmToken ?? "")")
        
        // Send token to server
        if let token = fcmToken {
            sendTokenToServer(token)
        }
    }
    
    // MARK: - UNUserNotificationCenterDelegate
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Show notification when app is in foreground
        completionHandler([[.banner, .badge, .sound]])
    }
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        
        // Handle notification tap
        if let routeId = userInfo["routeId"] as? String,
           let routeInt = Int(routeId) {
            NotificationCenter.default.post(
                name: .init("OpenRoute"),
                object: nil,
                userInfo: ["routeId": routeInt, "direction": userInfo["direction"]]
            )
        }
        
        completionHandler()
    }
    
    // MARK: - Server Communication
    
    private func sendTokenToServer(_ token: String) {
        guard let url = URL(string: "https://your-api.com/api/registerToken") else { return }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "token": token,
            "platform": "ios",
            "userId": UIDevice.current.identifierForVendor?.uuidString ?? UUID().uuidString
        ]
        
        request.httpBody = try? JSONSerialization.data(withJSONObject: body)
        
        URLSession.shared.dataTask(with: request).resume()
    }
    
    func subscribeToRoute(_ routeId: Int) {
        // Subscribe to FCM topic
        Messaging.messaging().subscribe(toTopic: "route_\(routeId)") { error in
            if let error = error {
                print("Failed to subscribe: \(error)")
            } else {
                print("Subscribed to route_\(routeId)")
            }
        }
        
        // Update server
        updateSubscriptions()
    }
    
    func unsubscribeFromRoute(_ routeId: Int) {
        Messaging.messaging().unsubscribe(fromTopic: "route_\(routeId)")
        updateSubscriptions()
    }
    
    private func updateSubscriptions() {
        // Get subscribed routes from UserDefaults and send to server
        // Implementation depends on your storage structure
    }
    
    func scheduleReminder(routeId: Int, direction: String, time: String) {
        guard let url = URL(string: "https://your-api.com/api/scheduleBusReminder") else { return }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "token": fcmToken ?? "",
            "routeId": routeId,
            "direction": direction,
            "departureTime": time,
            "reminderMinutes": [10, 5, 1]
        ]
        
        request.httpBody = try? JSONSerialization.data(withJSONObject: body)
        
        URLSession.shared.dataTask(with: request).resume()
    }
}

// AppDelegate extension for handling device token
extension AppDelegate {
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("Failed to register for push: \(error)")
    }
}
