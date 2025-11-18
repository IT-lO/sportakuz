(function(){
    const defaultConfig = {
        main_title: "Moje rezerwacje",
        cancel_button_text: "Anuluj rezerwację",
        confirm_message: "Czy na pewno chcesz anulować tę rezerwację?",
        background_color: "#ffffff",
        text_color: "#333333",
        primary_action_color: "#1e40af",
        accent_action_color: "#3b82f6",
        font_family: "Segoe UI",
        font_size: 16
    };

    let currentReservationCard = null;
    let bookings = Array.isArray(window.__bookings) ? window.__bookings : [];

    const BookingsPage = {
        showCancelModal(button){
            currentReservationCard = button.closest('.reservation-card');
            const modal = document.getElementById('cancelModal');
            const message = document.getElementById('modalMessage');
            const cfg = window.elementSdk ? window.elementSdk.config : defaultConfig;
            if(message) message.textContent = cfg.confirm_message || defaultConfig.confirm_message;
            if(modal) modal.style.display = 'flex';
        },
        closeCancelModal(){
            const modal = document.getElementById('cancelModal');
            if(modal) modal.style.display = 'none';
            currentReservationCard = null;
        },
        confirmCancel(){
            if(!currentReservationCard){ this.closeCancelModal(); return; }
            const bookingId = currentReservationCard.getAttribute('data-booking-id');
            const modalMsg = document.getElementById('modalMessage');
            // optimistic UI feedback
            currentReservationCard.style.opacity = '0.6';
            currentReservationCard.style.transform = 'scale(0.96)';
            fetch('/SportakUZ_war_exploded/api/bookings/cancel', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ bookingId })
            }).then(r => r.json().then(body => ({ok:r.ok, body})))
              .then(result => {
                  if(result.ok){
                      currentReservationCard.remove();
                      this.checkEmptyState();
                  } else {
                      if(modalMsg){ modalMsg.textContent = (result.body && result.body.error) ? result.body.error : 'Nie udało się anulować.'; }
                  }
                  this.closeCancelModal();
              }).catch(e => {
                  console.error('Cancel error', e);
                  if(modalMsg){ modalMsg.textContent = 'Błąd połączenia.'; }
                  this.closeCancelModal();
              });
        },
        checkEmptyState(){
            const list = document.getElementById('reservationsList');
            if(!list) return;
            const cards = list.querySelectorAll('.reservation-card');
            if(cards.length === 0){
                list.innerHTML = '<div class="empty-state"><div class="empty-icon">📅</div><h3>Brak aktywnych rezerwacji</h3><p>Nie masz obecnie żadnych zarezerwowanych zajęć sportowych.</p></div>';
            }
        }
    };

    window.BookingsPage = BookingsPage;

    function applyConfig(cfg){
        document.querySelectorAll('.cancel-button').forEach(btn => {
            btn.textContent = cfg.cancel_button_text || defaultConfig.cancel_button_text;
        });
    }

    function initElementSdk(){
        if(!window.elementSdk) return;
        return window.elementSdk.init({
            defaultConfig,
            onConfigChange: async (config) => applyConfig(config),
            mapToCapabilities: (config) => ({
                recolorables: [
                    colorCap(config,'background_color'),
                    colorCap(config,'text_color'),
                    colorCap(config,'primary_action_color'),
                    colorCap(config,'accent_action_color')
                ],
                borderables: [],
                fontEditable: fontCap(config),
                fontSizeable: fontSizeCap(config)
            }),
            mapToEditPanelValues: (config) => new Map([
                ['main_title', config.main_title || defaultConfig.main_title],
                ['cancel_button_text', config.cancel_button_text || defaultConfig.cancel_button_text],
                ['confirm_message', config.confirm_message || defaultConfig.confirm_message]
            ])
        });
    }

    function colorCap(cfg, prop){ return { get: () => cfg[prop] || defaultConfig[prop], set: (v) => { cfg[prop]=v; window.elementSdk && window.elementSdk.setConfig({ [prop]: v }); } }; }
    function fontCap(cfg){ return { get: () => cfg.font_family || defaultConfig.font_family, set: (v) => { cfg.font_family=v; window.elementSdk && window.elementSdk.setConfig({ font_family: v }); } }; }
    function fontSizeCap(cfg){ return { get: () => cfg.font_size || defaultConfig.font_size, set: (v) => { cfg.font_size=v; window.elementSdk && window.elementSdk.setConfig({ font_size: v }); } }; }

    function wireModalBackground(){
        const modal = document.getElementById('cancelModal');
        if(modal){ modal.addEventListener('click', (e) => { if(e.target === modal){ BookingsPage.closeCancelModal(); } }); }
        document.addEventListener('keydown', (e) => { if(e.key === 'Escape'){ BookingsPage.closeCancelModal(); } });
    }

    document.addEventListener('DOMContentLoaded', () => {
        wireModalBackground();
        initElementSdk();
        applyConfig(defaultConfig);
        BookingsPage.checkEmptyState();
    });
})();
