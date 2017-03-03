$(function(){


var allPlugins = new Vue({
    el : '#plugin',
    data : {
        version : '',
        plugins : [],
        usedPlugins : [],
        unusedPlugins : [],
    },
    mounted: function(){
        renderData();
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
                renderData();
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
                renderData();
            })
        }
    }
})
function renderData(){
    $.get("../plugins",function(e){
        allPlugins.$set(allPlugins,'version','v'+e.pluginVersion);
        allPlugins.$set(allPlugins,'plugins',e.plugins);
        allPlugins.$set(allPlugins,'usedPlugins',e.usedPlugins);
        allPlugins.$set(allPlugins,'unusedPlugins',e.unusedPlugins);
    })
};
})