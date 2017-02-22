$(function(){
    $.get("../plugins",function(e){
        var pluginVersion = new Vue({
            el : '#pluginVersion',
            data : {
                message : e.pluginVersion
            },
        })

        var allPlugins = new Vue({
            el : '#allPlugins',
            data : {
                plugins : e.plugins,
                usedPlugins : e.usedPlugins,
                unusedPlugins : e.unusedPlugins,
            },
            methods:{
                getStartHref:function(val){
                    return "../"+val+"/start"
                },
                getViewHref:function(val){
                    return "../"+val+"/"
                },
                getStopHref:function(val){
                    return "../"+val+"/stop"
                },
                val(val){
                    return val;
                },
                stop:function(name){
                    $.get("../"+name+"/stop",function(e){
                        if(e.code == 0){
                            $.gritter.add({
                                title: "   "+e.message,
                                class_name: 'gritter-success' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                            });
                        }else{
                            $.gritter.add({
                                title: e.message,
                                class_name: 'gritter-error' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                            });
                        }
                    })
                },
                uninstall:function(name){
                    $.get("../"+name+"/uninstall",function(e){
                        if(e.code == 0){
                            $.gritter.add({
                                title:"   "+ e.message,
                                class_name: 'gritter-success' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                            });
                        }else{
                            $.gritter.add({
                                title: e.message,
                                class_name: 'gritter-error' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                            });
                        }
                    })
                }
            }
        })
    })
});