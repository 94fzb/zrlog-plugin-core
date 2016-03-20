$(function(){
	$(".stop").click(function(){
		$.get($(this).attr("name")+"/stop",function(e){
			if(e.code == 0){
				$.gritter.add({
					title: e.message,
					class_name: 'gritter-success' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
				});
			}else{
				$.gritter.add({
					title: '  发生了一些异常...',
					class_name: 'gritter-error' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
				});
			}
		})
	})
});