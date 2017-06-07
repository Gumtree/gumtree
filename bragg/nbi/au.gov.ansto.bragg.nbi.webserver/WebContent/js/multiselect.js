$.widget("shift.selectable", $.ui.selectable, {
    options: {},
    previousIndex: -1,
    currentIndex: -1,
    _create: function() {
        var self = this;

        $.ui.selectable.prototype._create.call(this);

        $(this.element).on('selectableselecting', function(event, ui){
            self.currentIndex = $(ui.selecting.tagName, event.target).index(ui.selecting);
            if(event.shiftKey && self.previousIndex > -1) {
                $(ui.selecting.tagName, event.target).slice(Math.min(self.previousIndex, self.currentIndex), 1 + Math.max(self.previousIndex, self.currentIndex)).addClass('ui-selected');
                self.previousIndex = -1;
            } else {
                self.previousIndex = self.currentIndex;
            }
        });
    },
    destroy: function() {
        $.ui.selectable.prototype.destroy.call(this);
    },
    _setOption: function() {
        $.ui.selectable.prototype._setOption.apply(this, arguments);
    }
});

