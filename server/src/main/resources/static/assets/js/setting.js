$(function(){
    $.get("../setting",function(e){
        $("#autoDownloadLostFile").bootstrapSwitch('state',e.autoDownloadLostFile);
        $("#autoDownloadLostFile").attr("value",e.autoDownloadLostFile);
    })

    $('#autoDownloadLostFile').on('switchChange.bootstrapSwitch', function (event, state) {
        $("#autoDownloadLostFileVal").attr("value",state?"on":"off");
    });
    $(".btn-info").click(function(){

        var formId="ajax"+$(this).attr("id");
        $.post('../settingUpdate',$("#"+formId).serialize(),function(data){
            if(!data.code){
                $.gritter.add({
                    title: '  操作成功...',
                    class_name: 'gritter-success' + (!$('#gritter-success').get(0).checked ? ' gritter-success' : ''),
                });
            }else{
                $.gritter.add({
                    title: '  发生了一些异常...',
                    class_name: 'gritter-error' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                });
            }
        });
    });
});