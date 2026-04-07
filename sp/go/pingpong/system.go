package tdpnet

type System struct {
	*StandardSession
	Config SystemConfig
}

func NewSystem(config SystemConfig, connectNow bool) *System {
	s := &System{
		StandardSession: NewStandardSession(),
		Config:          config,
	}

	if connectNow {
		// In a real Go app, you would pass config.Security.CA to a tls.Config
		s.Connect(config.Host)
	}
	return s
}

func (s *System) OnConnected(timestamp int64) {
	// Automatically sign in upon connection
	s.Signin(s.Config.Credential)
}

// Placeholder for granted/denied logic
func (s *System) OnGranted(data interface{}) {}
func (s *System) OnDenied(data interface{})  {}
