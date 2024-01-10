$xml = @"
<toast activationType="protocol" launch="%url%" >
  <visual>
    <binding template="ToastGeneric">
      <text>%title%</text>
      <text>%content%</text>
    </binding>
  </visual>
  <audio src="ms-winsoundevent:Notification.Mail"/>
</toast>
"@
$XmlDocument = [Windows.Data.Xml.Dom.XmlDocument, Windows.Data.Xml.Dom.XmlDocument, ContentType = WindowsRuntime]::New()
$XmlDocument.loadXml($xml)
$AppId = 'GallAlarm'
[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime]::CreateToastNotifier($AppId).Show($XmlDocument)