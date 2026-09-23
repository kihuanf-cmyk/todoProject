document.addEventListener('DOMContentLoaded', function () {
    const calendarEl = document.getElementById('calendar');
    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'ko',
        events: [...window._todoEvents, ...window._googleCalendarEvents],

        // 일정 클릭
        eventClick: function (info) {
            const eventId = info.event.id;
            if (eventId.startsWith('todo-')) {
                const todoId = eventId.replace('todo-', '');
                location.href = "/todo/" + todoId + "?prev_url=/calendar";
            }
        }
    });
    calendar.render();
});

