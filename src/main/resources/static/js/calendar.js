(function(){
    const lang = window.__locale || 'pl';

    const dictionary = {
        pl: {
            bookBtn: "Zarezerwuj",
            spots: "Miejsca:",
            limit: "Osiągnięto limit 999 rezerwacji. Usuń niektóre rezerwacje.",
            success: "Rezerwacja została potwierdzona!",
            error: "Wystąpił błąd. Spróbuj ponownie.",
            connError: "Wystąpił błąd połączenia."
        },
        en: {
            bookBtn: "Book now",
            spots: "Spots:",
            limit: "Reservation limit (999) reached. Please cancel some bookings.",
            success: "Reservation confirmed!",
            error: "An error occurred. Please try again.",
            connError: "Connection error."
        }
    };

    const t = dictionary[lang] || dictionary['pl'];

    const defaultConfig = {
        background_color: "#ffffff",
        surface_color: "#f3f4f6",
        text_color: "#333333",
        primary_action_color: "#1e40af",
        secondary_action_color: "#3b82f6",
        font_family: "Segoe UI",
        font_size: 16,
        page_title: "Kalendarz Zajęć Sportowych",
        booking_button_text: t.bookBtn
    };

    let currentWeekStart = new Date();
    const day = currentWeekStart.getDay();
    const diff = currentWeekStart.getDate() - day + (day === 0 ? -6 : 1);
    currentWeekStart.setDate(diff);

    let selectedClass = null;
    let userReservations = [];

    const classes = window.__activities;
    if(window.__activities == null){
        console.log("Brak danych activities");
    }

    const dataHandler = {
        onDataChanged(data) { userReservations = data; }
    };

    async function initializeApp() {
        if (window.dataSdk) {
            try {
                const initResult = await window.dataSdk.init(dataHandler);
                if (!initResult.isOk) console.error("Failed to initialize data SDK");
            } catch(e){ console.error("dataSdk init error", e); }
        }
        if (window.elementSdk) {
            await window.elementSdk.init({
                defaultConfig,
                onConfigChange: async (config) => applyConfig(config),
                mapToCapabilities: (config) => ({
                    recolorables: [
                        colorCap(config, 'background_color'),
                        colorCap(config, 'surface_color'),
                        colorCap(config, 'text_color'),
                        colorCap(config, 'primary_action_color'),
                        colorCap(config, 'secondary_action_color')
                    ],
                    borderables: [],
                    fontEditable: fontCap(config),
                    fontSizeable: fontSizeCap(config)
                }),
                mapToEditPanelValues: (config) => new Map([
                    ["page_title", config.page_title || defaultConfig.page_title],
                    ["booking_button_text", config.booking_button_text || defaultConfig.booking_button_text]
                ])
            });
        }
        renderCalendar();
        updateWeekDisplay();
    }

    function colorCap(config, prop){
        return { get: () => config[prop] || defaultConfig[prop], set: (v) => { config[prop] = v; if(window.elementSdk) window.elementSdk.setConfig({ [prop]: v }); } };
    }
    function fontCap(config){ return { get: () => config.font_family || defaultConfig.font_family, set: (v) => { config.font_family = v; if(window.elementSdk) window.elementSdk.setConfig({ font_family: v }); } }; }
    function fontSizeCap(config){ return { get: () => config.font_size || defaultConfig.font_size, set: (v) => { config.font_size = v; if(window.elementSdk) window.elementSdk.setConfig({ font_size: v }); } }; }

    function applyConfig(config){
        const customFont = config.font_family || defaultConfig.font_family;
        const baseSize = config.font_size || defaultConfig.font_size;
        const baseFontStack = 'Tahoma, Geneva, Verdana, sans-serif';
        document.body.style.fontFamily = `${customFont}, ${baseFontStack}`;
        document.body.style.background = config.background_color || defaultConfig.background_color;
        document.body.style.color = config.text_color || defaultConfig.text_color;
        document.body.style.fontSize = `${baseSize}px`;
        const title = document.getElementById('page-title');
        if(title && window.elementSdk){
            title.textContent = config.page_title || defaultConfig.page_title;
        }
        if(title){
            title.style.color = config.primary_action_color || defaultConfig.primary_action_color;
            title.style.fontSize = `${baseSize * 2.25}px`;
        }

        const subtitle = document.querySelector('.header p');
        if(subtitle){
            subtitle.style.color = config.text_color || defaultConfig.text_color;
            subtitle.style.fontSize = `${baseSize}px`;
        }
        document.querySelectorAll('.nav-button').forEach(btn => { btn.style.background = config.primary_action_color || defaultConfig.primary_action_color; btn.style.fontSize = `${baseSize}px`; });
        const wd = document.querySelector('.week-display');
        if(wd){ wd.style.color = config.primary_action_color || defaultConfig.primary_action_color; wd.style.fontSize = `${baseSize * 1.25}px`; }
        document.querySelectorAll('.calendar-header-cell').forEach(cell => { cell.style.color = config.primary_action_color || defaultConfig.primary_action_color; cell.style.background = config.surface_color || defaultConfig.surface_color; cell.style.fontSize = `${baseSize * 0.875}px`; });
        document.querySelectorAll('.time-cell').forEach(cell => { cell.style.color = config.text_color || defaultConfig.text_color; cell.style.fontSize = `${baseSize * 0.875}px`; });
        document.querySelectorAll('.class-card').forEach(card => { card.style.background = `linear-gradient(135deg, ${config.secondary_action_color || defaultConfig.secondary_action_color} 0%, ${config.primary_action_color || defaultConfig.primary_action_color} 100%)`; });
        const mt = document.getElementById('modal-title');
        if(mt){ mt.style.color = config.primary_action_color || defaultConfig.primary_action_color; mt.style.fontSize = `${baseSize * 1.5}px`; }
        document.querySelectorAll('.detail-label').forEach(l => l.style.fontSize = `${baseSize * 0.875}px`);
        document.querySelectorAll('.detail-value').forEach(v => { v.style.color = config.text_color || defaultConfig.text_color; v.style.fontSize = `${baseSize * 0.875}px`; });
        document.querySelectorAll('.form-label').forEach(l => { l.style.color = config.text_color || defaultConfig.text_color; l.style.fontSize = `${baseSize * 0.875}px`; });
        document.querySelectorAll('.form-input').forEach(i => i.style.fontSize = `${baseSize * 0.875}px`);
        document.querySelectorAll('.btn-primary').forEach(b => { b.style.background = config.primary_action_color || defaultConfig.primary_action_color; b.style.fontSize = `${baseSize}px`; });
        document.querySelectorAll('.btn-secondary').forEach(b => { b.style.background = config.surface_color || defaultConfig.surface_color; b.style.color = config.text_color || defaultConfig.text_color; b.style.fontSize = `${baseSize}px`; });

        const confirmBtn = document.getElementById('confirm-booking');
        if(confirmBtn && window.elementSdk){
            confirmBtn.textContent = config.booking_button_text || defaultConfig.booking_button_text;
        }
    }

    function renderCalendar(){
        const calendarHeader = document.getElementById('calendar-header');
        const calendarBody = document.getElementById('calendar-body');
        if(!calendarHeader || !calendarBody) return;
        calendarHeader.innerHTML = '';
        calendarBody.innerHTML = '';

        for(let day=0; day<7; day++){
            const date = new Date(currentWeekStart);
            date.setDate(date.getDate() + day);
            const iso = date.getFullYear() + '-' + String(date.getMonth()+1).padStart(2,'0') + '-' + String(date.getDate()).padStart(2,'0');

            let dayName = date.toLocaleDateString(lang, { weekday: 'long' });
            dayName = dayName.charAt(0).toUpperCase() + dayName.slice(1);

            const dayHeader = document.createElement('div');
            dayHeader.className = 'calendar-header-cell';
            dayHeader.textContent = `${dayName} ${date.getDate()}.${date.getMonth()+1}`;
            calendarHeader.appendChild(dayHeader);

            const dayColumn = document.createElement('div'); dayColumn.className = 'day-column';

            const dayClasses = classes
                .filter(c => (c.date ? c.date === iso : c.day === day))
                .sort((a,b)=>a.time.localeCompare(b.time));

            dayClasses.forEach(classData => {
                const classCard = document.createElement('div'); classCard.className = 'class-card';
                const instructorLine = (classData.isSubstitution && classData.substitutedFor)
                    ? `<div class="class-info">👤 <s>${classData.instructor}</s></div>`
                    : `<div class="class-info">👤 ${classData.instructor}</div>`;
                const substitutionLine = (classData.isSubstitution && classData.substitutedFor)
                    ? `<div class="class-info">🔄 ${classData.substitutedFor}</div>`
                    : '';

                classCard.innerHTML = `<div class="class-name">${classData.name}</div>
<div class="class-info">🕐 ${classData.time} (${classData.duration} min)</div>
${instructorLine}
${substitutionLine}
<div class="class-info">📍 ${t.spots} ${classData.spots}</div>`;

                classCard.addEventListener('click', () => openModal(classData, day));
                dayColumn.appendChild(classCard);
            });
            calendarBody.appendChild(dayColumn);
        }
    }

    function updateWeekDisplay(){
        const endDate = new Date(currentWeekStart); endDate.setDate(endDate.getDate() + 6);
        const startStr = `${currentWeekStart.getDate()}.${currentWeekStart.getMonth()+1}.${currentWeekStart.getFullYear()}`;
        const endStr = `${endDate.getDate()}.${endDate.getMonth()+1}.${endDate.getFullYear()}`;
        const wd = document.getElementById('week-display');
        if(wd) wd.textContent = `${startStr} - ${endStr}`;
    }

    function openModal(classData, day){
        selectedClass = { ...classData, day };
        const date = new Date(currentWeekStart); date.setDate(date.getDate() + day);
        const map = {
            'modal-title': classData.name,
            'modal-date': `${date.getDate()}.${date.getMonth()+1}.${date.getFullYear()}`,
            'modal-time': `${classData.time} (${classData.duration} min)`,
            'modal-room': classData.room,
            'modal-instructor': classData.instructor,
            'modal-spots': classData.spots,
            'modal-level': classData.level,
            'modal-substituted-for': classData.isSubstitution && classData.substitutedFor ? classData.substitutedFor : ''
        };
        Object.entries(map).forEach(([id,val]) => { const el = document.getElementById(id); if(el) el.textContent = val; });
        const substitutionRow = document.getElementById('row-substituted-for');
        if(substitutionRow){
            if(classData.isSubstitution && classData.substitutedFor){
                substitutionRow.style.display='flex';
            } else {
                substitutionRow.style.display='none';
            }
        }
        const successEl = document.getElementById('success-message'); if(successEl) successEl.style.display='none';
        const modal = document.getElementById('modal'); if(modal) modal.classList.add('active');
    }

    function handleBooking(){
        const messageEl = document.getElementById('success-message');
        if(userReservations.length >= 999){ showMessage(messageEl, t.limit ,'#ef4444'); return; }

        const confirmBtn = document.getElementById('confirm-booking');
        if(!confirmBtn) return;
        const originalText = confirmBtn.textContent;
        confirmBtn.disabled = true; confirmBtn.innerHTML = `${originalText}<span class="loading"></span>`;
        const date = new Date(currentWeekStart); date.setDate(date.getDate() + selectedClass.day);

        fetch('/SportakUZ_war_exploded/api/bookings/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ classId: selectedClass.id})
        }).then(r => r.json().then(body => ({ ok: r.ok, body })))
            .then(result => {
                confirmBtn.disabled = false; confirmBtn.textContent = originalText;
                if(result.ok){
                    showMessage(messageEl, t.success ,'#10b981');
                    const nameInput = document.getElementById('user-name'); if(nameInput) nameInput.value='';
                    if(result.body && result.body.spots){
                        const idx = classes.findIndex(c => c.id === selectedClass.id);
                        if(idx !== -1){
                            classes[idx].spots = result.body.spots;
                        }
                        renderCalendar();
                    }
                } else {
                    const msg = (result.body && result.body.error) ? result.body.error : t.error;
                    showMessage(messageEl,msg,'#ef4444');
                }
            }).catch(e => {
            console.error('Reservation error', e);
            confirmBtn.disabled=false;
            confirmBtn.textContent = originalText;
            showMessage(messageEl, t.connError ,'#ef4444');
        });
    }

    function showMessage(el, text, bg){ if(!el) return; el.textContent = text; el.style.background = bg; el.style.display='block'; }

    function wireEvents(){
        const prev = document.getElementById('prev-week'); if(prev) prev.addEventListener('click', () => { currentWeekStart.setDate(currentWeekStart.getDate() - 7); renderCalendar(); updateWeekDisplay(); });
        const next = document.getElementById('next-week'); if(next) next.addEventListener('click', () => { currentWeekStart.setDate(currentWeekStart.getDate() + 7); renderCalendar(); updateWeekDisplay(); });
        const close = document.getElementById('close-modal'); if(close) close.addEventListener('click', () => { const modal = document.getElementById('modal'); if(modal) modal.classList.remove('active'); });
        const modalRoot = document.getElementById('modal'); if(modalRoot) modalRoot.addEventListener('click', (e) => { if(e.target.id === 'modal'){ modalRoot.classList.remove('active'); } });
        const confirmBtn = document.getElementById('confirm-booking'); if(confirmBtn) confirmBtn.addEventListener('click', handleBooking);
    }

    document.addEventListener('DOMContentLoaded', () => { wireEvents(); initializeApp(); });
})();